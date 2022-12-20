package com.husk.bookmarket.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.husk.bookmarket.databinding.FragmentChatViewBinding
import com.husk.bookmarket.model.ChatMessage


class ChatViewFragment : Fragment() {


    private var _binding: FragmentChatViewBinding? = null;
    private val viewModel: ChatViewModel by activityViewModels();

    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()


    private val texts = ArrayList<ChatMessage>()
    private val adapter = ChatMessageAdapter(texts, this)


    private var registration: ListenerRegistration? = null;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatViewBinding.inflate(inflater, container, false)


        binding.recyclerView.adapter = adapter

        viewModel.thread.observe(viewLifecycleOwner) { thread ->
            val user = Firebase.auth.currentUser!!
            if (thread.userAvatar != null) {
                Glide.with(this).load(thread.userAvatar).into(binding.profile)
                binding.profile.visibility = View.VISIBLE
            } else {
                binding.profile.visibility = View.INVISIBLE
            }
            binding.username.text = thread.userName

            binding.sendButton.setOnClickListener {
                if (binding.sendText.text.isEmpty()) {
                    return@setOnClickListener
                }
                val doc = db.collection("chats/${thread.threadId}/chats").document()
                val newText = binding.sendText.text.toString()
                val msg = ChatMessage(
                    doc.id,
                    Firebase.auth.currentUser!!.uid,
                    thread.userId,
                    newText,
                    Timestamp.now(),
                    false
                );
                binding.sendButton.isClickable = false
                val user = Firebase.auth.currentUser!!
                val u = db.collection("chat_threads/${user.uid}/threads").document(thread.threadId)
                val v =
                    db.collection("chat_threads/${thread.userId}/threads").document(thread.threadId)
                db.runBatch { batch ->
                    batch.update(u, "lastText", newText)
                    batch.update(v, "lastText", newText)
                    batch.set(doc, msg.toMap())
                }.addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "Couldn't send message ${it.exception?.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        binding.sendButton.isClickable = true
                    } else {
                        binding.sendText.text.clear()
                        binding.sendButton.isClickable = true
                    }
                };
            }
            registration?.remove()
            registration = db.collection("chats/${thread.threadId}/chats")
                .orderBy("time", Query.Direction.ASCENDING).addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "Error occurred: ${e.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        for (dc in snapshots!!.documentChanges) {
                            if (dc.type == DocumentChange.Type.ADDED) {
                                val text = ChatMessage.fromDoc(dc.document)
                                text.isCurrentUser =
                                    text.senderId.equals(Firebase.auth.currentUser!!.uid)
                                texts.add(text)
                                adapter.notifyItemInserted(texts.size - 1);
                            }
                        }
                    }
                }
        }


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).apply{
            supportActionBar?.hide()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as AppCompatActivity).apply{
            supportActionBar?.show()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView();
        _binding = null;
        registration?.remove()
        texts.clear()
    }
}