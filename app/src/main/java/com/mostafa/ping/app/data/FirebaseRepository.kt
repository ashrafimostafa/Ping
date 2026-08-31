package com.mostafa.ping.app.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
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

class FirebaseRepository {

    private val auth get() = Firebase.auth
    private val db get() = Firebase.firestore
    private val devices get() = db.collection("devices")
    private val inboxes get() = db.collection("inboxes")

    suspend fun signIn(): String {
        val current = auth.currentUser
        if (current != null) return current.uid
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: error("Anonymous sign-in failed")
    }

    suspend fun loadOrCreateProfile(uid: String): DeviceProfile {
        val existing = devices.whereEqualTo("uid", uid).limit(1).get().await()
        if (!existing.isEmpty) {
            val snap = existing.documents.first()
            val code = snap.id
            subscribe(code)
            return DeviceProfile(
                code = code,
                uid = uid,
                partnerCode = snap.getString("partnerCode")?.takeIf { it.isNotBlank() }
            )
        }

        var code = PairCode.random()
        repeat(8) {
            val collision = devices.document(code).get().await()
            if (!collision.exists()) {
                devices.document(code).set(
                    mapOf(
                        "uid" to uid,
                        "partnerCode" to null,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                subscribe(code)
                return DeviceProfile(code = code, uid = uid, partnerCode = null)
            }
            code = PairCode.random()
        }
        error("Could not allocate a Ping ID")
    }

    suspend fun pairWith(myCode: String, rawPartnerCode: String): DeviceProfile {
        val partnerCode = PairCode.normalize(rawPartnerCode)
        require(PairCode.isValid(partnerCode)) { "IDs are 6 letters and numbers" }
        require(partnerCode != myCode) { "That's your own ID" }

        val partnerSnap = devices.document(partnerCode).get().await()
        require(partnerSnap.exists()) { "No device found for $partnerCode" }

        val myRef = devices.document(myCode)
        val partnerRef = devices.document(partnerCode)
        val myUid = auth.currentUser?.uid ?: error("Not signed in")
        val partnerUid = partnerSnap.getString("uid") ?: error("Partner record is incomplete")

        db.runBatch { batch ->
            batch.set(
                myRef,
                mapOf("uid" to myUid, "partnerCode" to partnerCode),
                SetOptions.merge()
            )
            batch.set(
                partnerRef,
                mapOf("uid" to partnerUid, "partnerCode" to myCode),
                SetOptions.merge()
            )
        }.await()

        return DeviceProfile(code = myCode, uid = myUid, partnerCode = partnerCode)
    }

    suspend fun unpair(myCode: String, partnerCode: String?) {
        val myRef = devices.document(myCode)
        db.runBatch { batch ->
            batch.set(myRef, mapOf("partnerCode" to null), SetOptions.merge())
            if (!partnerCode.isNullOrBlank()) {
                batch.set(
                    devices.document(partnerCode),
                    mapOf("partnerCode" to null),
                    SetOptions.merge()
                )
            }
        }.await()
    }

    suspend fun sendPing(fromCode: String, toCode: String) {
        inboxes.document(toCode).collection("pings").add(
            mapOf(
                "fromCode" to fromCode,
                "message" to "I love you ❤️",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        if (FcmSender.isConfigured) {
            FcmSender.sendLove(targetCode = toCode, fromCode = fromCode)
        }
    }

    fun listenPartner(myCode: String, onChange: (String?) -> Unit): ListenerRegistration {
        return devices.document(myCode).addSnapshotListener { snap, _ ->
            onChange(snap?.getString("partnerCode")?.takeIf { it.isNotBlank() })
        }
    }

    fun listenInbox(myCode: String, onPing: (IncomingPing) -> Unit): ListenerRegistration {
        var primed = false
        return inboxes.document(myCode)
            .collection("pings")
            .orderBy("createdAt")
            .limitToLast(1)
            .addSnapshotListener { snap, _ ->
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

    private fun subscribe(code: String) {
        Firebase.messaging.subscribeToTopic(PairCode.topic(code))
    }
}
