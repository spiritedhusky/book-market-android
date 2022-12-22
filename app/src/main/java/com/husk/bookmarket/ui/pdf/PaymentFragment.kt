package com.husk.bookmarket.ui.pdf

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.husk.bookmarket.R
import com.husk.bookmarket.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PdfViewModel by activityViewModels()

    private val dbRef = FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)

        viewModel.pdf.observe(viewLifecycleOwner) { pdf ->
            binding.bookTitle.text = pdf.title
            binding.bookPrice.text = String.format("%.2f BDT", pdf.price)
        }

        binding.payButton.setOnClickListener {
            val name = binding.cardHolder.text.toString().isNotEmpty()
            val num = binding.cardNumber.text.toString().matches(Regex("[0-9]{16}"))
            val exp = binding.cardExpiry.text.toString().matches(Regex("[0-9]{1,2}/[0-9]{2}"))
            val cvc = binding.cardCVC.text.toString().matches(Regex("[0-9]{3}"))

            if(!(name && num && exp && cvc)){
                Snackbar.make( requireActivity().findViewById(android.R.id.content),
                    "Invalid Payment Details",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (viewModel.pdf.value != null) {
                val user = Firebase.auth.currentUser!!.uid
                binding.progressBar.visibility = View.VISIBLE
                binding.payButton.isClickable = false
                dbRef.collection("pdfs/${viewModel.pdf.value!!.pdfId}/owners")
                    .add(mapOf("userId" to user)).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Snackbar.make( requireActivity().findViewById(android.R.id.content),
                            "Payment Successful",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    } else {
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "Payment failed: ${it.exception?.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        binding.progressBar.visibility = View.GONE
                        binding.payButton.isClickable = true
                    }
                }
            }
        }

        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}