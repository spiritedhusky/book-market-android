package com.husk.bookmarket.ui.pdf

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.husk.bookmarket.R
import com.husk.bookmarket.databinding.FragmentPdfDetailBinding
import com.husk.bookmarket.model.Pdf

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private val ARG_PDF: Pdf? = null

/**
 * A simple [Fragment] subclass.
 * Use the [PdfDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PdfDetailFragment : Fragment() {

    private var _binding: FragmentPdfDetailBinding? = null

    private val binding get() = _binding!!

    private val viewModel: PdfViewModel by activityViewModels()

    private val storageRef = Firebase.storage.reference
    private val dbRef = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPdfDetailBinding.inflate(layoutInflater, container, false)

        viewModel.pdf.observe(viewLifecycleOwner) { pdf ->
            binding.bookAuthor.text = pdf.posterName
            binding.bookDescription.text = pdf.description
            binding.bookTitle.text = pdf.title
            Log.e("IMAGE", pdf.image!!)
            binding.bookImage.visibility = View.VISIBLE
            Glide.with(this).load(storageRef.child(pdf.image!!)).into(binding.bookImage)

            val user = Firebase.auth.currentUser!!.uid
            viewModel.userOwnsBook.value = false
            if (user == pdf.posterId) {
                viewModel.userOwnsBook.value = true
            } else {
                dbRef.collection("pdfs/${pdf.pdfId}/owners")
                    .whereEqualTo("userId", user).get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            viewModel.userOwnsBook.value = it.result.size() > 0
                        } else {
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                "Error: ${it.exception?.message}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        viewModel.userOwnsBook.observe(viewLifecycleOwner) { owns ->
            if (owns) {
                binding.purchaseReadButton.text = "Read"
                binding.purchaseReadButton.setOnClickListener {
                    findNavController().navigate(R.id.action_pdfDetailFragment2_to_pdfViewFragment)
                }
            } else {
                binding.purchaseReadButton.text = "Purchase ${viewModel.pdf.value?.price} RS"
                binding.purchaseReadButton.setOnClickListener {
                    findNavController().navigate(R.id.action_pdfDetailFragment2_to_paymentFragment)
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}