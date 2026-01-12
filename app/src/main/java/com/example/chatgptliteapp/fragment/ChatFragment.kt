package com.example.chatgptliteapp.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptliteapp.R
import com.example.chatgptliteapp.adapter.ChatAdapter
import com.example.chatgptliteapp.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment(R.layout.chat_fragment) {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private lateinit var database: DatabaseReference
    private lateinit var adapter: ChatAdapter
    private val chatList = mutableListOf<ChatMessage>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ===== INIT VIEW =====
        rvChat = view.findViewById(R.id.rvChat)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)

        // ===== RECYCLER VIEW =====
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        rvChat.layoutManager = layoutManager

        adapter = ChatAdapter(chatList)
        rvChat.adapter = adapter

        // ===== FIREBASE =====
        database = FirebaseDatabase.getInstance()
            .getReference("chat_messages")

        // ===== SEND MESSAGE =====
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            // 1) PESAN USER
            val userMessage = ChatMessage(
                senderId = FirebaseAuth.getInstance().uid,
                message = text,
                timestamp = System.currentTimeMillis(),
                senderType = "user"
            )
            database.push().setValue(userMessage)
            etMessage.text.clear()

            // 2) PESAN INTERAKSI BOT (PANJANG & NATURAL)
            val typingMessages = listOf(
                "AI sedang menganalisis pertanyaan Anda dan menyiapkan respons terbaik. Mohon tunggu sebentar...",
                "Sistem AI sedang memproses input Anda dengan mempertimbangkan konteks dan informasi yang relevan. Proses ini hanya memerlukan beberapa detik.",
                "Terima kasih atas pertanyaannya. Saya sedang mempelajarinya dan akan segera memberikan jawaban yang paling sesuai."
            )

            val typingRef = database.push()
            typingRef.setValue(
                ChatMessage(
                    senderId = "bot",
                    message = typingMessages.random(),
                    timestamp = System.currentTimeMillis(),
                    senderType = "bot"
                )
            )

            // 3) SIMULASI JAWABAN FINAL (NANTI DIGANTI CHATGPT API)
            rvChat.postDelayed({
                typingRef.setValue(
                    ChatMessage(
                        senderId = "bot",
                        message = "Berikut penjelasan singkatnya. Jika Anda ingin detail lebih lanjut, silakan tanyakan kembali.",
                        timestamp = System.currentTimeMillis(),
                        senderType = "bot"
                    )
                )
            }, 2200) // durasi interaksi
        }

        // ===== REALTIME LISTENER (RINGAN & STABIL) =====
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chat = snapshot.getValue(ChatMessage::class.java)
                if (chat != null) {
                    chatList.add(chat)
                    adapter.notifyItemInserted(chatList.size - 1)
                    rvChat.scrollToPosition(chatList.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
