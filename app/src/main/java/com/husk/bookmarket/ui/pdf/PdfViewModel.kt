package com.husk.bookmarket.ui.pdf

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PdfViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the pdf Fragment"
    }
    val text: LiveData<String> = _text
}