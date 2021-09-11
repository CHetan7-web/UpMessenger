package com.example.upmessenger.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.upmessenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MessagesActivity extends AppCompatActivity {

    FirebaseUser currUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        TextView key = findViewById(R.id.chat_message);

        currUser = FirebaseAuth.getInstance().getCurrentUser();
        getSupportActionBar().hide();
        //Toast.makeText(this,"Messages VIew",Toast.LENGTH_SHORT).show();
        Intent intent = this.getIntent();
        String gameId = intent.getStringExtra("userKey");
        //Toast.makeText(this,"Current user : "+currUser.getUid()+"\nSelected User : "+gameId,1).show();

        key.setText(gameId);

        //key.setText(gameId);
    }
}