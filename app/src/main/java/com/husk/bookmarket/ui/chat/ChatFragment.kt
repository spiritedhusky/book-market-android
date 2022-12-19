package com.husk.bookmarket.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.husk.bookmarket.databinding.FragmentChatBinding
import com.husk.bookmarket.model.ChatThread
import com.husk.bookmarket.model.Post

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val threads = ArrayList<ChatThread>()
    private val adapter = ChatThreadAdapter(threads, this);

    private lateinit var registration: ListenerRegistration;

    val viewModel: ChatViewModel by activityViewModels();

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val chatViewModel =
            ViewModelProvider(this).get(ChatViewModel::class.java)

        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.recyclerView.adapter = adapter

        val user = Firebase.auth.currentUser!!;
        registration = db.collection("chat_threads/${user.uid}/threads")
            .orderBy("lastActive", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Error occurred: ${e.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    for (dc in snapshots!!.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            threads.add(ChatThread.fromDoc(dc.document))
                            adapter.notifyItemInserted(threads.size - 1)
                        }
                    }
                }
            }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        registration.remove()
        threads.clear()
    }
}