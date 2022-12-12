package com.husk.bookmarket.model

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class Post(
    val postId: String,
    val posterId: String,
    val posterAvatar: Uri?,
    val title: String,
    val author: String,
    val description: String?,
    val image: Uri?,
    val time: Timestamp
) {
    companion object Factory {
        fun fromDoc(doc: DocumentSnapshot): Post {
            var avatar: Uri? = null
            var image: Uri? = null
            doc.getString("postAvatar")?.let { avatar = Uri.parse(it) }
            doc.getString("image")?.let { image = Uri.parse(it) }
            return Post(
                doc.id,
                doc.getString("posterId")!!,
                avatar,
                doc.getString("title")!!,
                doc.getString("author")!!,
                doc.getString("description"),
                image,
                doc.getTimestamp("timestamp")!!
            )
        }
    }

    fun toDoc(doc: DocumentReference): Task<Void> {
        return doc.set(
            mapOf(
                "postId" to postId,
                "posterId" to posterId,
                "posterAvatar" to posterAvatar,
                "title" to title,
                "author" to author,
                "description" to description,
                "image" to image.toString(),
                "timestamp" to Timestamp.now()
            )
        );
    }
}