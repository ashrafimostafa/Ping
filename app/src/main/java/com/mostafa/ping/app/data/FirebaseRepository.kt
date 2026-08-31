package com.mostafa.ping.app.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.mostafa.ping.app.IncomingPing
import kotlinx.coroutines.tasks.await

data class DeviceProfile(
    val code: String,
    val uid: String,
    val partnerCode: String?,
)

class FirebaseRepository(
    private val appContext: Context
) {
    private val db by lazy {
        Firebase.firestore.apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
        }
    }
    private val devices get() = db.collection("devices")
    private val inboxes get() = db.collection("inboxes")

    fun deviceUid(): String = DeviceIdStore.getOrCreateUid(appContext)

    suspend fun loadOrCreateProfile(uid: String): DeviceProfile {
        val savedCode = DeviceIdStore.getSavedCode(appContext)
        if (savedCode != null) {
            return try {
                val snap = devices.document(savedCode).get().await()
                if (snap.exists()) {
                    registerPush(savedCode)
                    return DeviceProfile(
                        code = savedCode,
                        uid = uid,
                        partnerCode = snap.getString("partnerCode")?.takeIf { it.isNotBlank() }
                    )
                }
                createDevice(uid, preferredCode = savedCode)
            } catch (e: Exception) {
                Log.e(TAG, "load saved profile failed", e)
                throw friendly(e)
            }
        }

        return try {
            createDevice(uid)
        } catch (e: Exception) {
            Log.e(TAG, "create profile failed", e)
            throw friendly(e)
        }
    }

    private suspend fun createDevice(uid: String, preferredCode: String? = null): DeviceProfile {
        var code = preferredCode?.takeIf { PairCode.isValid(it) } ?: PairCode.random()
        repeat(12) {
            val ref = devices.document(code)
            val collision = ref.get().await()
            if (!collision.exists() || collision.getString("uid") == uid) {
                ref.set(
                    mapOf(
                        "uid" to uid,
                        "partnerCode" to collision.getString("partnerCode"),
                        "createdAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).await()
                DeviceIdStore.saveCode(appContext, code)
                registerPush(code)
                val partner = if (collision.exists()) {
                    collision.getString("partnerCode")?.takeIf { it.isNotBlank() }
                } else {
                    null
                }
                return DeviceProfile(code = code, uid = uid, partnerCode = partner)
            }
            code = PairCode.random()
        }
        error("Could not allocate a Ping ID")
    }

    suspend fun pairWith(myCode: String, rawPartnerCode: String): DeviceProfile {
        val partnerCode = PairCode.normalize(rawPartnerCode)
        require(PairCode.isValid(partnerCode)) { "IDs are 6 letters and numbers" }
        require(partnerCode != myCode) { "That's your own ID" }

        try {
            val partnerSnap = devices.document(partnerCode).get().await()
            require(partnerSnap.exists()) { "No device found for $partnerCode" }

            val myUid = deviceUid()
            val partnerUid = partnerSnap.getString("uid") ?: error("Partner record is incomplete")

            db.runBatch { batch ->
                batch.set(
                    devices.document(myCode),
                    mapOf("uid" to myUid, "partnerCode" to partnerCode),
                    SetOptions.merge()
                )
                batch.set(
                    devices.document(partnerCode),
                    mapOf("uid" to partnerUid, "partnerCode" to myCode),
                    SetOptions.merge()
                )
            }.await()
            registerPush(myCode)

            return DeviceProfile(code = myCode, uid = myUid, partnerCode = partnerCode)
        } catch (e: Exception) {
            if (e is IllegalArgumentException) throw e
            throw friendly(e)
        }
    }

    suspend fun unpair(myCode: String, partnerCode: String?) {
        try {
            db.runBatch { batch ->
                batch.set(devices.document(myCode), mapOf("partnerCode" to null), SetOptions.merge())
                if (!partnerCode.isNullOrBlank()) {
                    batch.set(
                        devices.document(partnerCode),
                        mapOf("partnerCode" to null),
                        SetOptions.merge()
                    )
                }
            }.await()
        } catch (e: Exception) {
            throw friendly(e)
        }
    }

    suspend fun sendPing(fromCode: String, toCode: String) {
        try {
            inboxes.document(toCode).collection("pings").add(
                mapOf(
                    "fromCode" to fromCode,
                    "message" to "I love you ❤️",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val partnerToken = devices.document(toCode).get().await()
                .getString("fcmToken")
                ?.takeIf { it.isNotBlank() }

            FcmSender.sendLove(
                targetCode = toCode,
                fromCode = fromCode,
                targetToken = partnerToken
            )
        } catch (e: Exception) {
            throw friendly(e)
        }
    }

    fun listenPartner(myCode: String, onChange: (String?) -> Unit): ListenerRegistration {
        return devices.document(myCode).addSnapshotListener { snap, error ->
            if (error != null) {
                Log.e(TAG, "partner listener", error)
                return@addSnapshotListener
            }
            onChange(snap?.getString("partnerCode")?.takeIf { it.isNotBlank() })
        }
    }

    fun listenInbox(myCode: String, onPing: (IncomingPing) -> Unit): ListenerRegistration {
        var primed = false
        return inboxes.document(myCode)
            .collection("pings")
            .orderBy("createdAt")
            .limitToLast(1)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e(TAG, "inbox listener", error)
                    return@addSnapshotListener
                }
                if (snap == null) return@addSnapshotListener
                if (!primed) {
                    primed = true
                    return@addSnapshotListener
                }
                val doc = snap.documentChanges.lastOrNull()?.document ?: return@addSnapshotListener
                val from = doc.getString("fromCode") ?: return@addSnapshotListener
                if (from == myCode) return@addSnapshotListener
                onPing(
                    IncomingPing(
                        fromCode = from,
                        message = doc.getString("message") ?: "I love you ❤️",
                        id = doc.id
                    )
                )
            }
    }

    suspend fun saveFcmToken(token: String) {
        val code = DeviceIdStore.getSavedCode(appContext) ?: return
        devices.document(code).set(
            mapOf(
                "fcmToken" to token,
                "fcmUpdatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        Log.d(TAG, "Saved FCM token for $code")
    }

    private suspend fun registerPush(code: String) {
        try {
            Firebase.messaging.subscribeToTopic(PairCode.topic(code)).await()
            Log.d(TAG, "Subscribed to ${PairCode.topic(code)}")
        } catch (e: Exception) {
            Log.e(TAG, "Topic subscribe failed", e)
        }
        try {
            val token = Firebase.messaging.token.await()
            devices.document(code).set(
                mapOf(
                    "fcmToken" to token,
                    "fcmUpdatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            Log.d(TAG, "Registered FCM token (${token.take(12)}…)")
        } catch (e: Exception) {
            Log.e(TAG, "FCM token register failed", e)
        }
    }

    private fun friendly(e: Exception): Exception {
        val msg = buildString {
            append(e.message ?: e.javaClass.simpleName)
            e.cause?.message?.let { append(" | ").append(it) }
        }
        return Exception(msg, e)
    }

    companion object {
        private const val TAG = "PingFirebase"
    }
}
