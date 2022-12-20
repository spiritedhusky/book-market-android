package com.husk.bookmarket.ui.chat

import com.husk.bookmarket.ui.home.HomeFragment
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.husk.bookmarket.GlideApp
import com.husk.bookmarket.R
import com.husk.bookmarket.databinding.ChatEntryBinding
import com.husk.bookmarket.databinding.PostCardBinding
import com.husk.bookmarket.model.ChatThread
import com.husk.bookmarket.model.Pdf
import com.husk.bookmarket.model.Post

class ChatThreadAdapter(private val threads: ArrayList<ChatThread>, private val fragment: ChatFragment) :
    RecyclerView.Adapter<ChatThreadAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */


    class ViewHolder(private val binding: ChatEntryBinding, private val fragment: ChatFragment) :
        RecyclerView.ViewHolder(binding.root) {
        private val storageRef = Firebase.storage.reference
        fun bind(thread: ChatThread) {
            val user = Firebase.auth.currentUser!!
            if (thread.userAvatar != null) {
                Glide.with(fragment).load(thread.userAvatar).into(binding.profile)
                binding.profile.visibility = View.VISIBLE
            } else {
                binding.profile.visibility = View.INVISIBLE
            }
            binding.username.text = thread.userName
            binding.lastText.text = thread.lastText
            binding.root.setOnClickListener{
                fragment.viewModel.thread.value = thread
                fragment.findNavController().navigate(R.id.action_navigation_chat_to_chatViewFragment)
            }
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = ChatEntryBinding.inflate(inflater, viewGroup, false)

        return ViewHolder(binding, fragment)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(threads[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = threads.size

}
