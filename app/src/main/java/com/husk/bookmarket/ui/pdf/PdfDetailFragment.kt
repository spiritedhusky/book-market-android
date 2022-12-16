package com.husk.bookmarket.ui.pdf

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
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

    private val userOwnsBook = MutableLiveData<Boolean>().apply {
        value = false
    }

    private val storageRef = Firebase.storage.reference

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
        }

        userOwnsBook.observe(viewLifecycleOwner) { owns ->
            if (owns) {
                binding.purchaseReadButton.text = "Read"
                binding.purchaseReadButton.setOnClickListener {

                }
            } else {
                binding.purchaseReadButton.text = "Purchase ${viewModel.pdf.value?.price} RS"
                binding.purchaseReadButton.setOnClickListener {

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