package com.husk.bookmarket.model

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class ChatMessage(
    val messageId: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val time: Timestamp,
    var isCurrentUser: Boolean
) {
    companion object Factory {
        fun fromDoc(doc: DocumentSnapshot): ChatMessage {
            return ChatMessage(
                doc.id,
                doc.getString("senderId")!!,
                doc.getString("receiverId")!!,
                doc.getString("content")!!,
                doc.getTimestamp("time")!!,
                false
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
            "messageId" to messageId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to content,
            "time" to Timestamp.now()
        )
    }
}