package com.husk.bookmarket.ui.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.husk.bookmarket.R
import com.husk.bookmarket.Utils
import com.husk.bookmarket.databinding.FragmentAddPostBinding
import com.husk.bookmarket.model.Post
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
        imagePickerActivity.launch(i)
    }

    private var imagePickerActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            binding.apply {
                this.image.setImageURI(imageUri)
                this.imageCard.visibility = View.VISIBLE
            }
        }
    }


    private fun processResult(task: Task<Void>) {
        if (!task.isSuccessful) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "Couldn't upload post. Error occurred: ${task.exception?.message}",
                Snackbar.LENGTH_SHORT
            ).show()
            binding.postButton.isClickable = true
            binding.progressBar.visibility = View.GONE
        } else {
            findNavController().navigate(R.id.action_navigation_add_post_to_navigation_home)
        }
    }

    private fun submitPost() {
        val user = Firebase.auth.currentUser!!
        val title = binding.bookTitle.text.toString()
        val author = binding.bookAuthor.text.toString()
        val description = binding.bookDescription.text.toString()
        if (title.isEmpty() || author.isEmpty()) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "Title or author cannot be empty",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.postButton.isClickable = false

        val imagePath = imageUri?.let { "image/${UUID.randomUUID()}.png" }
        val docRef = postRef.document()
        val newPost = Post(
            docRef.id,
            user.uid,
            user.displayName!!,
            user.photoUrl,
            title,
            author,
            description,
            imagePath,
            Timestamp.now()
        )
        // try and upload image first
        if (imagePath != null) {
            val data = Utils.processImage(requireActivity().contentResolver, imageUri!!, 600, 450)
            val ref = storageRef.child(imagePath)
            ref.putBytes(data).continueWithTask {
                if (!it.isSuccessful) {
                    it.exception?.apply { throw this }
                }
                newPost.toDoc(docRef)
            }.addOnCompleteListener {
                processResult(it)
            }
        } else {
            newPost.toDoc(docRef).addOnCompleteListener {
                processResult(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.addImageButton.setOnClickListener {
            imageChooser()
        }

        binding.postButton.setOnClickListener {
            submitPost()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}