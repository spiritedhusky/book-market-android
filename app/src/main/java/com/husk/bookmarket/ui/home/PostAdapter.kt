package com.husk.bookmarket.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.husk.bookmarket.GlideApp
import com.husk.bookmarket.R
import com.husk.bookmarket.Utils
import com.husk.bookmarket.databinding.PostCardBinding
import com.husk.bookmarket.model.ChatThread
import com.husk.bookmarket.model.Pdf
import com.husk.bookmarket.model.Post

class PostAdapter(private val posts: ArrayList<Post>, private val fragment: HomeFragment) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    class ViewHolder(private val binding: PostCardBinding, private val fragment: HomeFragment) :
        RecyclerView.ViewHolder(binding.root) {
        private val storageRef = Firebase.storage.reference

        val db = FirebaseFirestore.getInstance()

        /* Search for existing thread between users, create one if for both sides if not present */
        private fun selectThread(post: Post) {
            val user = Firebase.auth.currentUser!!
            val receiverId = post.posterId

            db.collection("chat_threads/${user.uid}/threads").whereEqualTo("userId", receiverId)
                .get().addOnCompleteListener {
                    if (!it.isSuccessful) {
                        fragment.showError(it.exception)
                        binding.chatButton.isClickable = true
                        return@addOnCompleteListener
                    }
                    if (it.result.isEmpty) {
                        // create threads
                        val u = db.collection("chat_threads/${user.uid}/threads").document()
                        val v = db.collection("chat_threads/${receiverId}/threads").document(u.id)
                        val threadU = ChatThread(
                            u.id,
                            receiverId,
                            post.posterName,
                            post.posterAvatar,
                            null,
                            Timestamp.now()
                        )
                        val threadV = ChatThread(
                            v.id, user.uid, user.displayName!!, user.photoUrl, null, Timestamp.now()
                        )
                        db.runBatch { batch ->
                            batch.set(u, threadU.toMap())
                            batch.set(v, threadV.toMap())
                        }.addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                fragment.showError(task.exception)
                                binding.chatButton.isClickable = true
                            } else {
                                fragment.viewModel.thread.value = threadU
                                fragment.findNavController()
                                    .navigate(R.id.action_navigation_home_to_chatViewFragment)
                            }
                        }
                    } else {
                        fragment.viewModel.thread.value = ChatThread.fromDoc(it.result.documents[0])
                        fragment.findNavController()
                            .navigate(R.id.action_navigation_home_to_chatViewFragment)
                    }
                }
        }

        private fun deletePost(post: Post) {
            db.collection("posts").document(post.postId).delete().addOnCompleteListener {
                if (!it.isSuccessful) {
                    binding.deleteButton.isClickable = true
                    Utils.showSnackBar(
                        fragment.requireActivity(),
                        "Failed to delete post: ${it.exception?.message}"
                    )
                } else {
                    Utils.showToast(fragment.requireActivity(), "Successfully deleted post")
                }
            }
        }

        fun bind(post: Post) {
            if (post.posterAvatar != null) {
                Glide.with(fragment).load(post.posterAvatar).into(binding.profileImage)
                binding.profileImage.visibility = View.VISIBLE
            } else {
                binding.profileImage.visibility = View.GONE
            }
            binding.username.text = post.posterName
            binding.bookTitle.text = post.title
            binding.bookAuthor.text = post.author
            binding.bookDescription.text = post.description
            if (post.image != null) {
                Log.e("Image", post.image)
                Glide.with(fragment).load(storageRef.child(post.image)).into(binding.bookImage);
                binding.bookImage.visibility = View.VISIBLE
            } else {
                binding.bookImage.visibility = View.GONE
            }
            val user = Firebase.auth.currentUser!!
            if (post.posterId == user.uid) {
                binding.chatButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE
                binding.deleteButton.isClickable = true
                binding.deleteButton.setOnClickListener {
                    binding.deleteButton.isClickable = false
                    deletePost(post)
                }
            } else {
                binding.deleteButton.visibility = View.GONE
                binding.chatButton.visibility = View.VISIBLE
                binding.chatButton.isClickable = true
                binding.chatButton.setOnClickListener {
                    binding.chatButton.isClickable = false
                    selectThread(post)
                }
            }
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = PostCardBinding.inflate(inflater, viewGroup, false)

        return ViewHolder(binding, fragment)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(posts[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = posts.size

}