package com.husk.bookmarket.model

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class Post(
    val postId: String,
    val posterId: String,
    val posterName: String,
    val posterAvatar: Uri?,
    val title: String,
    val author: String,
    val description: String?,
    val image: String?,
    val time: Timestamp
) {
    companion object Factory {
        fun fromDoc(doc: DocumentSnapshot): Post {
            var avatar: Uri? = null
            doc.getString("posterAvatar")?.let { avatar = Uri.parse(it) }
            return Post(
                doc.id,
                doc.getString("posterId")!!,
                doc.getString("posterName")!!,
                avatar,
                doc.getString("title")!!,
                doc.getString("author")!!,
                doc.getString("description"),
                doc.getString("image"),
                doc.getTimestamp("timestamp")!!
            )
        }
    }

    fun toDoc(doc: DocumentReference): Task<Void> {
        return doc.set(
            mapOf(
                "postId" to postId,
                "posterId" to posterId,
                "posterName" to posterName,
                "posterAvatar" to posterAvatar,
                "title" to title,
                "author" to author,
                "description" to description,
                "image" to image,
                "timestamp" to Timestamp.now()
            )
        );
    }
}