package com.husk.bookmarket.ui.pdf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.husk.bookmarket.R
import com.husk.bookmarket.databinding.FragmentPdfBinding
import com.husk.bookmarket.model.Pdf
import com.husk.bookmarket.model.Post
import com.husk.bookmarket.ui.home.PostAdapter
import java.io.File

class PdfFragment : Fragment() {

    private var _binding: FragmentPdfBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val pdfRef = Firebase.storage.reference.child("pdf/design.pdf")

    val viewModel: PdfViewModel by activityViewModels()

    private val pdfs: ArrayList<Pdf> = ArrayList();
    private val adapter = PdfAdapter(pdfs, this)

    private val pdfsRef = FirebaseFirestore.getInstance().collection("pdfs")

    private lateinit var registration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.recyclerView.adapter = adapter

        registration = pdfsRef.orderBy("timestamp", Query.Direction.ASCENDING)
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
                            pdfs.add(0, Pdf.fromDoc(dc.document))
                            adapter.notifyItemInserted(0)
                        } else if (dc.type == DocumentChange.Type.REMOVED) {
                            var del = -1
                            pdfs.forEachIndexed { idx, pdf ->
                                if (pdf.pdfId == dc.document.id) {
                                    del = idx
                                }
                            }
                            if (del >= 0) {
                                pdfs.removeAt(del)
                                adapter.notifyItemRemoved(del)
                            }
                        }
                    }
                }
            }

        binding.addPdfButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_pdf_to_addPdfFragment)
        }

        return root
    }

    public fun setPdf(pdf: Pdf) {
        viewModel.pdf.value = pdf
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        registration.remove()
        pdfs.clear()
    }
}