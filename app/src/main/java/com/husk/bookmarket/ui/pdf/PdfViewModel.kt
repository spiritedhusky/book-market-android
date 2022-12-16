package com.husk.bookmarket.ui.pdf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.husk.bookmarket.model.Pdf

class PdfViewModel : ViewModel() {

    private val _pdf = MutableLiveData<Pdf>().apply {
        value = null
    }
    val pdf: MutableLiveData<Pdf> = _pdf
}