package com.husk.bookmarket.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.husk.bookmarket.GlideApp
import com.husk.bookmarket.R
import com.husk.bookmarket.SignInActivity
import com.husk.bookmarket.Utils
import com.husk.bookmarket.databinding.FragmentProfileBinding
import java.util.UUID


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val viewModel: ProfileViewModel by activityViewModels()

    private fun imageChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        imagePickerActivity.launch(i)
    }

    private var imageUri: Uri? = null;

    private val db = FirebaseFirestore.getInstance()
    private val store = Firebase.storage.reference

    private var imagePickerActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.apply {
                profile.setImageURI(imageUri)
            }
        }
    }

    private fun handleError(t: Task<QuerySnapshot>) {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            "Error occurred: ${t.exception?.message}",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun handleError(e: java.lang.Exception?) {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            "Error occurred: ${e?.message}",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun updateField(
        doc: DocumentSnapshot, field: String, value: String, tasks: ArrayList<Task<Void>>
    ) {
        if (doc.getString(field) != value) {
            val task = doc.reference.update(field, value)
            tasks.add(task)
        }
    }

    private fun updateField(
        doc: DocumentReference, field: String, value: String, tasks: ArrayList<Task<Void>>
    ) {
        val task = doc.update(field, value)
        tasks.add(task)
    }

    fun updateUserName(name: String, retry: Int) {
        val user = Firebase.auth.currentUser!!
        val tasks = ArrayList<Task<Void>>();
        var fail = false
        db.collection("chat_threads/${user.uid}/threads").get().addOnCompleteListener {
            if (!it.isSuccessful) {
                handleError(it)
                fail = true
                return@addOnCompleteListener
            }
            for (doc in it.result.documents) {
                val v = db.document("chat_threads/${doc.getString("userId")}/threads/${doc.id}")
                updateField(v, "userName", name, tasks);
            }
        }
        db.collection("pdfs").whereEqualTo("posterId", user.uid).get().addOnCompleteListener {
            if (!it.isSuccessful) {
                handleError(it)
                fail = true
                return@addOnCompleteListener
            }
            for (doc in it.result.documents) {
                updateField(doc, "posterName", name, tasks);
            }
        }
        db.collection("posts").whereEqualTo("posterId", user.uid).get().addOnCompleteListener {
            if (!it.isSuccessful) {
                handleError(it)
                fail = true
                return@addOnCompleteListener
            }
            for (doc in it.result.documents) {
                updateField(doc, "posterName", name, tasks);
            }
        }
        db.collection("users").whereEqualTo("uid", user.uid).get().addOnCompleteListener {
            if (!it.isSuccessful) {
                handleError(it)
                fail = true
                return@addOnCompleteListener
            }
            for (doc in it.result.documents) {
                updateField(doc, "name", name, tasks);
            }
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener {
            if (!it.isSuccessful) {
                if (retry > 0) {
                    updateUserName(name, retry - 1);
                } else {
                    handleError(Exception("Failed to update username"))
                    enable()
                }
            } else {
                if (!fail) {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Successfully updated username",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    enable()
                } else {
                    updateProfile(name, retry - 1)
                }

            }
        }
    }

    fun updateProfile(image: String, retry: Int) {
        val user = Firebase.auth.currentUser!!
        val tasks = ArrayList<Task<Void>>();
        var fail = false;
        db.collection("chat_threads/${user.uid}/threads").get().addOnCompleteListener {
            if (!it.isSuccessful) {
                handleError(it)
                fail = true
                return@addOnCompleteListener
            }
            for (doc in it.result.documents) {
                val v = db.document("chat_threads/${doc.getString("userId")}/threads/${doc.id}")
                updateField(v, "userAvatar", image, tasks);
            }
        }
        db.collection("pdfs").whereEqualTo("posterId", user.uid).get().addOnCompleteListener {
            if (!it.isSuccessful) {
                handleError(it)
                fail = true
                return@addOnCompleteListener
            }
            for (doc in it.result.documents) {
                updateField(doc, "posterAvatar", image, tasks);
            }
        }
        db.collection("posts").whereEqualTo("posterId", user.uid).get().addOnCompleteListener {
            if (!it.isSuccessful) {
                handleError(it)
                fail = true
                return@addOnCompleteListener
            }
            for (doc in it.result.documents) {
                updateField(doc, "posterAvatar", image, tasks);
            }
        }
        db.collection("users").whereEqualTo("uid", user.uid).get().addOnCompleteListener {
            if (!it.isSuccessful) {
                handleError(it)
                fail = true
                return@addOnCompleteListener
            }
            for (doc in it.result.documents) {
                updateField(doc, "photo", image, tasks);
            }
        }

        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    if (retry > 0) {
                        updateProfile(image, retry - 1);
                    } else {
                        handleError(Exception("Failed to update profile image"))
                        enable()
                    }
                } else {
                    if (!fail) {
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "Successfully updated profile",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        enable()
                    } else {
                        updateProfile(image, retry - 1)
                    }
                }
            }
    }

    fun update(name: String, image: Uri?) {
        val user = Firebase.auth.currentUser!!
        val profileUpdates = userProfileChangeRequest {
            displayName = name.ifEmpty { user.displayName }
            photoUri = image ?: user.photoUrl
        }
        user.updateProfile(profileUpdates).addOnCompleteListener {
            if (!it.isSuccessful) {
                handleError(it.exception)
                enable()
                return@addOnCompleteListener
            }
            updateUserName(name, 20)
            updateProfile(image.toString(), 20)
        }
    }

    private var enableCount = 0;
    private fun enable() {
        if(--enableCount <= 0){
            binding.progressBar2.visibility = View.GONE
            binding.updateButton.isClickable = true
        }
        enableCount = 0;
    }

    private fun disable(count: Int) {
        enableCount = count
        binding.progressBar2.visibility = View.VISIBLE
        binding.updateButton.isClickable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val user = Firebase.auth.currentUser!!
        binding.username.setText(user.displayName)

        if (user.photoUrl != null) {
            Glide.with(this).load(user.photoUrl).into(binding.profile)
        } else {
            binding.profile.setImageResource(R.drawable.profile_icon)
        }

        binding.logoutButton.setOnClickListener {
            AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                startActivity(Intent(requireContext(), SignInActivity::class.java))
                requireActivity().finish()
            }
        }

        binding.imageButton.setOnClickListener {
            imageChooser()
        }

        binding.updateButton.setOnClickListener {
            disable(1)
            if (imageUri != null) {
                val bytes =
                    Utils.processImage(requireActivity().contentResolver, imageUri!!, 500, 500)
                val store = store.child("avatars/${UUID.randomUUID()}.png")
                store.putBytes(bytes).continueWithTask() {
                    if (!it.isSuccessful) {
                        it.exception?.apply { throw this }
                    }
                    store.downloadUrl
                }.addOnCompleteListener {
                    if (!it.isSuccessful) {
                        handleError(it.exception)
                        enable()
                        return@addOnCompleteListener
                    }
                    disable(2)
                    update(binding.username.text.toString(), it.result)
                }
            } else {
                update(binding.username.text.toString(), null)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}