package com.husk.bookmarket.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.husk.bookmarket.R
import com.husk.bookmarket.databinding.PostCardBinding
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
        fun bind(post: Post) {
            if (post.posterAvatar != null) {
                binding.profileImage.setImageURI(post.posterAvatar)
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