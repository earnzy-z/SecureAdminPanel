package com.earnzy.app.Activity

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.earnzy.app.R
import com.earnzy.app.adapters.ChatMessageAdapter
import com.earnzy.app.models.ChatMessage
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class SupportChatActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var chatRecycler: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: MaterialButton
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_chat)
        
        toolbar = findViewById(R.id.toolbar)
        chatRecycler = findViewById(R.id.messages_recycler)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)
        
        toolbar.setNavigationOnClickListener { finish() }
        
        setupChat()
        
        sendButton.setOnClickListener {
            val text = messageInput.text.toString()
            if (text.isNotEmpty()) {
                sendMessage(text)
                messageInput.text.clear()
            }
        }
    }

    private fun setupChat() {
        chatRecycler.layoutManager = LinearLayoutManager(this)
        adapter = ChatMessageAdapter(messages)
        chatRecycler.adapter = adapter
        
        // Add welcome message
        messages.add(ChatMessage("Hello! How can I help you today?", false, System.currentTimeMillis()))
        adapter.notifyDataSetChanged()
    }

    private fun sendMessage(text: String) {
        messages.add(ChatMessage(text, true, System.currentTimeMillis()))
        adapter.notifyItemInserted(messages.size - 1)
        chatRecycler.scrollToPosition(messages.size - 1)
        
        // Simulate bot response
        simulateBotResponse()
    }

    private fun simulateBotResponse() {
        android.os.Handler(mainLooper).postDelayed({
            messages.add(ChatMessage("Thank you for your message. Our team will assist you shortly.", false, System.currentTimeMillis()))
            adapter.notifyItemInserted(messages.size - 1)
            chatRecycler.scrollToPosition(messages.size - 1)
        }, 1000)
    }
}
