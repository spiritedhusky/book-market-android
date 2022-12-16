package com.husk.bookmarket.ui.pdf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.husk.bookmarket.R
import com.husk.bookmarket.databinding.PdfCardBinding
import com.husk.bookmarket.model.Pdf
import com.husk.bookmarket.model.Post

class PdfAdapter(private val pdfs: ArrayList<Pdf>, private val fragment: PdfFragment) :
    RecyclerView.Adapter<PdfAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(private val binding: PdfCardBinding, private val fragment: PdfFragment) : RecyclerView.ViewHolder(binding.root) {

        private val storageRef = Firebase.storage.reference

        fun bind(pdf: Pdf) {
            if (pdf.posterAvatar != null) {
                binding.profileImage.setImageURI(pdf.posterAvatar)
                binding.profileImage.visibility = View.VISIBLE
            } else {
                binding.profileImage.visibility = View.GONE
            }
            binding.username.text = pdf.posterName
            binding.bookTitle.text = pdf.title
            binding.bookDescription.text = pdf.description
            if (pdf.image != null) {
                Glide.with(fragment).load(storageRef.child(pdf.image)).into(binding.bookImage);
                binding.bookImage.visibility = View.VISIBLE
            } else {
                binding.bookImage.visibility = View.GONE
            }

            binding.detailsButton.setOnClickListener {
                fragment.setPdf(pdf)
                fragment.findNavController().navigate(R.id.action_navigation_pdf_to_pdfDetailFragment2)
            }
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = PdfCardBinding.inflate(inflater, viewGroup, false)
        return ViewHolder(binding, fragment)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(pdfs[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = pdfs.size

}