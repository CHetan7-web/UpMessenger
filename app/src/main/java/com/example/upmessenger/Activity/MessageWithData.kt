package com.example.upmessenger.Activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.upmessenger.R

class MessageWithData : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_with_data)

        supportActionBar?.hide()

        val messageUri : Uri? = intent?.data
        val message : String? = intent?.getStringExtra(Intent.EXTRA_TEXT)

        Toast.makeText(applicationContext, intent?.type,Toast.LENGTH_SHORT).show()
        if (intent?.type == "text/plain") {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            if (message != null) {
//                intent?.extras
                Log.d("URI CONTEXT",message)
            }
            else
                Log.d("URI CONTEXT","NULL")
        }
    }
}
