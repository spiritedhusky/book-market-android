package com.husk.bookmarket.ui.chat

import com.husk.bookmarket.ui.home.HomeFragment
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
import com.husk.bookmarket.databinding.ChatEntryBinding
import com.husk.bookmarket.databinding.MessageBoxBinding
import com.husk.bookmarket.databinding.PostCardBinding
import com.husk.bookmarket.model.ChatMessage
import com.husk.bookmarket.model.ChatThread
import com.husk.bookmarket.model.Pdf
import com.husk.bookmarket.model.Post

class ChatMessageAdapter(private val texts: ArrayList<ChatMessage>, private val fragment: ChatViewFragment) :
    RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */


    class ViewHolder(private val binding: MessageBoxBinding, private val fragment: ChatViewFragment) :
        RecyclerView.ViewHolder(binding.root) {
        private val storageRef = Firebase.storage.reference
        fun bind(message: ChatMessage) {
            if(message.isCurrentUser){
                binding.cardL.visibility = View.GONE;
                binding.cardR.visibility = View.VISIBLE;
                binding.messageR.text = message.content
            } else {
                binding.cardR.visibility = View.GONE;
                binding.cardL.visibility = View.VISIBLE;
                binding.messageL.text = message.content
            }
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = MessageBoxBinding.inflate(inflater, viewGroup, false)

        return ViewHolder(binding, fragment)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(texts[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = texts.size

}
