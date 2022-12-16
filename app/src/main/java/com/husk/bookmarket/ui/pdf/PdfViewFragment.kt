package com.husk.bookmarket.ui.pdf

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.husk.bookmarket.R
import com.husk.bookmarket.databinding.FragmentPdfViewBinding
import java.io.File


class PdfViewFragment : Fragment() {

    private var _binding: FragmentPdfViewBinding? = null
    private val binding get() = _binding!!


    private val viewModel: PdfViewModel by activityViewModels()
    private val storageRef = Firebase.storage.reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPdfViewBinding.inflate(inflater, container, false)

        viewModel.pdf.observe(viewLifecycleOwner) { pdf ->
            binding.progressBar.visibility = View.VISIBLE
            val localFile = File.createTempFile("temp", "pdf")
            storageRef.child(pdf.pdf).getFile(localFile).addOnSuccessListener {
                // Local temp file has been created
                binding.pdfView.fromFile(localFile).show()
                binding.progressBar.visibility = View.GONE
                activity?.title = pdf.title
            }.addOnFailureListener {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Couldn't load PDF: ${it.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}