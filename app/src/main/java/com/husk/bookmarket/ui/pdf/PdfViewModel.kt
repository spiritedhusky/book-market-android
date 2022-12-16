package com.husk.bookmarket.ui.pdf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.husk.bookmarket.model.Pdf

class PdfViewModel : ViewModel() {

    val pdf = MutableLiveData<Pdf>().apply {
        value = null
    }
    val userOwnsBook = MutableLiveData<Boolean>().apply {
        value = false
    }
}