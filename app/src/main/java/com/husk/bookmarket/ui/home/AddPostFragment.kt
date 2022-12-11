package com.husk.bookmarket.ui.home

import android.R.attr.data
import android.R.attr.fragmentAllowEnterTransitionOverlap
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.husk.bookmarket.R
import com.husk.bookmarket.SignInActivity
import com.husk.bookmarket.databinding.FragmentAddPostBinding
import kotlinx.android.synthetic.main.fragment_add_post.*
import kotlinx.android.synthetic.main.fragment_add_post.view.*
import java.io.ByteArrayOutputStream
import java.util.UUID


class AddPostFragment : Fragment() {
    private var _binding: FragmentAddPostBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val storageRef = Firebase.storage.reference
    private val postRef = FirebaseFirestore.getInstance().collection("posts")

    private var imageUri: Uri? = null

    private fun imageChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        launchSomeActivity.launch(i)
    }

    private var launchSomeActivity = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            binding.root.apply {
                this.image.setImageURI(imageUri)
                this.image.visibility = View.VISIBLE
            }
        }
    }

    private fun submitPost() {
        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(requireActivity(), SignInActivity::class.java))
            return
        }
        val view = binding.root
        val title = view.titleEditText.text.toString()
        val author = view.authorEditText.text.toString()
        val description = view.descriptionEditText.text.toString()
        if (title.isEmpty() || author.isEmpty()) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "Title or author cannot be empty",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        progressBar.visibility = View.VISIBLE;
        // try and upload image first
        if (imageUri != null) {
            val bitmap = BitmapFactory.decodeStream(
                requireActivity().contentResolver.openInputStream(imageUri!!)
            )
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400 *(bitmap.height/bitmap.width), true)
            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 60, baos)
            val data = baos.toByteArray()

            val ref = storageRef.child("image/${UUID.randomUUID()}.png")
            ref.putBytes(data).continueWithTask {
                if (!it.isSuccessful) {
                    it.exception?.apply { throw this }
                }
                ref.downloadUrl
            }.continueWithTask {
                if (!it.isSuccessful) {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Couldn't upload image. Error occurred: ${it.exception?.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    it.exception?.apply { throw this }
                }
                val docRef = postRef.document()
                docRef.set(
                    mapOf(
                        "postId" to docRef.id,
                        "posterId" to Firebase.auth.currentUser?.uid,
                        "title" to title,
                        "author" to author,
                        "description" to description,
                        "image" to it.result.toString()
                    )
                )
            }.addOnCompleteListener {
                if (!it.isSuccessful) {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Couldn't upload post. Error occurred: ${it.exception?.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    findNavController().navigate(R.id.action_navigation_add_post_to_navigation_home)
                }
            }
        } else {
            val docRef = postRef.document()
            val newPost = mapOf(
                "postId" to docRef.id,
                "posterId" to Firebase.auth.currentUser?.uid,
                "title" to title,
                "author" to author,
                "description" to description,
                "image" to null
            )
            docRef.set(newPost).addOnCompleteListener {
                if (!it.isSuccessful) {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Couldn't upload post. Error occurred: ${it.exception?.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    findNavController().navigate(R.id.action_navigation_add_post_to_navigation_home)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        val root: View = binding.root

        root.addImageButton.setOnClickListener {
            imageChooser()
        }

        root.postButton.setOnClickListener {
            submitPost()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}