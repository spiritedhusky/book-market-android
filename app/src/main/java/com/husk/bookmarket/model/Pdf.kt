package com.husk.bookmarket.model

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class Pdf(
    val pdfId: String,
    val pdf: String,
    val posterId: String,
    val posterName: String,
    val posterAvatar: Uri?,
    val title: String,
    val description: String?,
    val price: Double,
    val image: String?,
    val time: Timestamp
) {
    companion object Factory {
        fun fromDoc(doc: DocumentSnapshot): Pdf {
            var avatar: Uri? = null
            doc.getString("postAvatar")?.let { avatar = Uri.parse(it) }
            return Pdf(
                doc.id,
                doc.getString("pdf")!!,
                doc.getString("posterId")!!,
                doc.getString("posterName")!!,
                avatar,
                doc.getString("title")!!,
                doc.getString("description"),
                doc.getDouble("price") ?: 0.0,
                doc.getString("image"),
                doc.getTimestamp("timestamp")!!
            )
        }
    }

    fun toDoc(doc: DocumentReference): Task<Void> {
        return doc.set(
            mapOf(
                "pdfId" to pdfId,
                "pdf" to pdf,
                "posterId" to posterId,
                "posterName" to posterName,
                "posterAvatar" to posterAvatar,
                "title" to title,
                "description" to description,
                "price" to price,
                "image" to image,
                "timestamp" to Timestamp.now()
            )
        );
    }
}