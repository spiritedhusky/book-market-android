package com.husk.bookmarket.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.husk.bookmarket.R
import com.husk.bookmarket.databinding.FragmentHomeBinding
import com.husk.bookmarket.model.Post

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val posts: ArrayList<Post> = ArrayList();
    private val adapter = PostAdapter(posts, this)

    private val postsRef = FirebaseFirestore.getInstance().collection("posts")

    private lateinit var registration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.addPostButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_home_to_add_post)
        }

        binding.recyclerView.adapter = adapter

        registration = postsRef.orderBy("timestamp", Query.Direction.DESCENDING)
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
                            posts.add(0, Post.fromDoc(dc.document))
                            adapter.notifyItemInserted(0)
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
    }
}