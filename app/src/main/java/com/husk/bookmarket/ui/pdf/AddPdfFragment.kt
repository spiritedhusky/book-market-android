package com.husk.bookmarket.ui.pdf

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.husk.bookmarket.R
import com.husk.bookmarket.Utils
import com.husk.bookmarket.databinding.FragmentAddPdfBinding
import com.husk.bookmarket.model.Pdf
import com.husk.bookmarket.model.Post
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.math.roundToInt

/**
 * A simple [Fragment] subclass.
 * Use the [AddPdfFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddPdfFragment : Fragment() {

    private var _binding: FragmentAddPdfBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val storageRef = Firebase.storage.reference

    private val db = FirebaseFirestore.getInstance().collection("pdfs")


    private var imageUri: Uri? = null
    private var pdfUri: Uri? = null

    private fun imageChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        imagePickerActivity.launch(i)
    }

    private fun pdfChooser() {
        val i = Intent()
        i.type = "application/pdf"
        i.action = Intent.ACTION_GET_CONTENT
        pdfPickerActivity.launch(i)
    }

    private var imagePickerActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.apply {
                bookImage.setImageURI(imageUri)
                imageCard.visibility = View.VISIBLE
            }
        }
    }

    private var pdfPickerActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            pdfUri = result.data?.data
            binding.apply {
                pdfFileName.text = Utils.getFileName(requireActivity().contentResolver, pdfUri!!)
            }
        }
    }


    private fun processResult(task: Task<Void>) {
        if (!task.isSuccessful) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "Couldn't upload pdf. Error occurred: ${task.exception?.message}",
                Snackbar.LENGTH_SHORT
            ).show()
            binding.postButton.isClickable = true
            binding.progressBar.visibility = View.GONE
        } else {
            findNavController().navigate(R.id.action_addPdfFragment_to_navigation_pdf)
        }
    }

    private fun submitPost() {
        val user = Firebase.auth.currentUser!!
        val title = binding.bookTitle.text.toString()
        val description = binding.bookDescription.text.toString()
        val price = run {
            try {
                binding.priceInput.text.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
        }
        if (title.isEmpty() || imageUri == null || pdfUri == null) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "Cannot leave out title, image or pdf",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        binding.postButton.isClickable = false

        val imagePath = "image/${UUID.randomUUID()}.png"
        val pdfPath = "pdf/${UUID.randomUUID()}.pdf"

        val docRef = db.document()
        val newPdf = Pdf(
            docRef.id,
            pdfPath,
            user.uid,
            user.displayName!!,
            user.photoUrl,
            title,
            description,
            price,
            imagePath,
            Timestamp.now()
        )
        val resolver = requireActivity().contentResolver
        val imageData = Utils.processImage(resolver, imageUri!!, 360, 480)
        val pdfData = Utils.readFile(resolver, pdfUri!!)

        val imageRef = storageRef.child(imagePath)
        val pdfRef = storageRef.child(pdfPath)

        imageRef.putBytes(imageData).continueWithTask {
            if (!it.isSuccessful) {
                it.exception?.apply { throw this }
            }
            pdfRef.putBytes(pdfData)
        }.continueWithTask() {
            if (!it.isSuccessful) {
                it.exception?.apply { throw this }
            }
            newPdf.toDoc(docRef)
        }.addOnCompleteListener {
            processResult(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddPdfBinding.inflate(inflater, container, false)

        binding.addImageButton.setOnClickListener {
            imageChooser()
        }
        binding.pdfButton.setOnClickListener {
            pdfChooser()
        }
        binding.postButton.setOnClickListener {
            submitPost()
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