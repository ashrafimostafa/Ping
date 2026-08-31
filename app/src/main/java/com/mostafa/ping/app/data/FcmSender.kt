package com.mostafa.ping.app.data

import android.util.Base64
import android.util.Log
import com.mostafa.ping.app.PingApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.concurrent.TimeUnit

/**
 * Sends FCM from the device using the HTTP v1 API.
 * Requires `app/src/main/assets/fcm-service-account.json`.
 */
object FcmSender {
    private const val TAG = "PingFcm"
    private const val ASSET_NAME = "fcm-service-account.json"
    private const val SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
    private const val TOKEN_URL = "https://oauth2.googleapis.com/token"
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val mutex = Mutex()
    private var account: ServiceAccount? = null
    private var accessToken: String? = null
    private var tokenExpiryEpochMs: Long = 0L

    val isConfigured: Boolean
        get() = loadAccount() != null

    suspend fun sendLove(targetCode: String, fromCode: String, targetToken: String? = null) {
        val acct = loadAccount()
            ?: error("Push not configured (missing $ASSET_NAME). Rebuild after adding the file.")
        val oauth = accessToken(acct)

        val errors = mutableListOf<String>()

        if (!targetToken.isNullOrBlank()) {
            runCatching {
                postMessage(acct.projectId, oauth, tokenTarget(targetToken, fromCode))
                Log.d(TAG, "FCM token send OK")
                return
            }.onFailure { errors += "token: ${it.message}" }
        }

        runCatching {
            postMessage(acct.projectId, oauth, topicTarget(targetCode, fromCode))
            Log.d(TAG, "FCM topic send OK")
            return
        }.onFailure { errors += "topic: ${it.message}" }

        error(errors.joinToString(" | ").ifBlank { "Notification send failed" })
    }

    private fun tokenTarget(token: String, fromCode: String): JSONObject =
        messageBody(fromCode).put("token", token)

    private fun topicTarget(targetCode: String, fromCode: String): JSONObject =
        messageBody(fromCode).put("topic", PairCode.topic(targetCode))

    private fun messageBody(fromCode: String): JSONObject =
        JSONObject()
            .put(
                "data",
                JSONObject()
                    .put("type", "love")
                    .put("fromCode", fromCode)
                    .put("title", "Ping")
                    .put("body", "I love you ❤️")
                    .put("message", "I love you ❤️")
            )
            .put(
                "android",
                JSONObject()
                    .put("priority", "HIGH")
                    .put("ttl", "120s")
                    .put("direct_boot_ok", true)
            )

    private suspend fun postMessage(projectId: String, oauth: String, message: JSONObject) {
        val body = JSONObject().put("message", message).toString()
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/v1/projects/$projectId/messages:send")
            .addHeader("Authorization", "Bearer $oauth")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody(jsonMedia))
            .build()

        withContext(Dispatchers.IO) {
            http.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                Log.d(TAG, "FCM response ${response.code}: $text")
                if (!response.isSuccessful) {
                    error("HTTP ${response.code}: $text")
                }
            }
        }
    }

    private fun loadAccount(): ServiceAccount? {
        account?.let { return it }
        return try {
            val app = PingApplication.instance
            app.assets.open(ASSET_NAME).bufferedReader().use { reader ->
                json.decodeFromString<ServiceAccount>(reader.readText())
            }.also { account = it }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load $ASSET_NAME", e)
            null
        }
    }

    private suspend fun accessToken(account: ServiceAccount): String = mutex.withLock {
        val now = System.currentTimeMillis()
        val cached = accessToken
        if (cached != null && now < tokenExpiryEpochMs - 60_000) {
            return cached
        }
        val assertion = signedJwt(account)
        val form = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=$assertion"
        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(form.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
            .build()
        val token = withContext(Dispatchers.IO) {
            http.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    error("OAuth token failed (${response.code}): $text")
                }
                JSONObject(text).getString("access_token")
            }
        }
        accessToken = token
        tokenExpiryEpochMs = now + 50 * 60 * 1000L
        token
    }

    private fun signedJwt(account: ServiceAccount): String {
        val nowSec = System.currentTimeMillis() / 1000L
        val header = base64Url("""{"alg":"RS256","typ":"JWT"}""")
        val claims = JSONObject()
            .put("iss", account.clientEmail)
            .put("scope", SCOPE)
            .put("aud", TOKEN_URL)
            .put("iat", nowSec)
            .put("exp", nowSec + 3600)
            .toString()
        val signingInput = "$header.${base64Url(claims)}"
        val key = parsePkcs8(account.privateKey)
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(key)
        signer.update(signingInput.toByteArray(Charsets.UTF_8))
        return "$signingInput.${base64Url(signer.sign())}"
    }

    private fun parsePkcs8(pem: String) = KeyFactory.getInstance("RSA").generatePrivate(
        PKCS8EncodedKeySpec(
            Base64.decode(
                pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\\n", "")
                    .replace("\n", "")
                    .replace("\r", "")
                    .trim(),
                Base64.DEFAULT
            )
        )
    )

    private fun base64Url(value: String): String = base64Url(value.toByteArray(Charsets.UTF_8))

    private fun base64Url(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

    @Serializable
    private data class ServiceAccount(
        @SerialName("project_id") val projectId: String,
        @SerialName("private_key") val privateKey: String,
        @SerialName("client_email") val clientEmail: String,
    )
}
