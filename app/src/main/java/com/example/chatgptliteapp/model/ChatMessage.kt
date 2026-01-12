package com.example.chatgptliteapp.model

data class ChatMessage(
    val senderId: String? = "",
    val message: String? = "",
    val timestamp: Long = 0L,
    val senderType: String = "user" // "user" atau "bot"
)
