package com.husk.bookmarket.model

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Objects

data class ChatThread(
    val threadId: String,
    val userId: String,
    val userName: String,
    val userAvatar: Uri?,
    val lastText: String?,
    val lastActive: Timestamp?
) {
    companion object Factory {
        fun fromDoc(doc: DocumentSnapshot): ChatThread {
            var avatar: Uri? = doc.getString("userAvatar")?.let { Uri.parse(it) }
            return ChatThread(
                doc.id,
                doc.getString("userId")!!,
                doc.getString("userName")!!,
                avatar,
                doc.getString("lastText"),
                doc.getTimestamp("lastActive")
            )
        }
    }

    fun toDoc(doc: DocumentReference): Task<Void> {
        return doc.set(
            toMap()
        );
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "threadId" to threadId,
            "userId" to userId,
            "userName" to userName,
            "userAvatar" to userAvatar.toString(),
            "lastText" to lastText,
            "lastActive" to Timestamp.now()
        )
    }
}