package com.husk.bookmarket.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.husk.bookmarket.model.ChatThread

class ChatViewModel : ViewModel() {
    val thread = MutableLiveData<ChatThread>().apply {
        value = null
    }
}