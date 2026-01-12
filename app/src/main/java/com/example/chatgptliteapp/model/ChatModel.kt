package com.example.chatgptliteapp.model

data class ChatModel(
    var id: String = "",
    var message: String = "",
    var role: String = "",
    var timestamp: Long = System.currentTimeMillis()
)