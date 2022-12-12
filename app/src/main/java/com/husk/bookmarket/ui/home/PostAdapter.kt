package com.husk.bookmarket.ui.home

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
import com.husk.bookmarket.R
import com.husk.bookmarket.model.Post
import kotlinx.android.synthetic.main.post_card.view.*

class PostAdapter(private val posts: ArrayList<Post>, private val fragment: HomeFragment) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profile: ImageView
        val username: TextView
        val title: TextView
        val author: TextView
        val description: TextView
        val image: ImageView
        val chatButton: Button

        init {
            profile = view.profileImage
            username = view.username
            title = view.bookTitle
            author = view.bookAuthor
            description = view.bookDescription
            image = view.bookImage
            chatButton = view.chatButton
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.post_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val post = posts[position]
        val user = Firebase.auth.currentUser!!
        if (post.posterAvatar != null) {
            viewHolder.profile.setImageURI(post.posterAvatar)
            viewHolder.image.visibility = View.VISIBLE
        } else {
            viewHolder.image.visibility = View.GONE
        }
        viewHolder.username.text = user.displayName
        viewHolder.title.text = post.title
        viewHolder.author.text = post.author
        viewHolder.description.text = post.description
        if (post.image != null) {
            Glide.with(fragment).load(post.image).into(viewHolder.image);
            viewHolder.image.visibility = View.VISIBLE
        } else {
            viewHolder.image.visibility = View.GONE
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = posts.size

}