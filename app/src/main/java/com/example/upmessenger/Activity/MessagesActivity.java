package com.example.upmessenger.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.upmessenger.Adapters.MessageAdapter;
import com.example.upmessenger.Models.UpMesssage;
import com.example.upmessenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessagesActivity extends AppCompatActivity {

    FirebaseUser currUser;
    FirebaseDatabase database;
    DatabaseReference senderRef,reciverRef;

    RecyclerView chatsRecycler ;
    MessageAdapter mMessageAdapter;

    EditText message;
    ImageView messageSend;

    ArrayList<UpMesssage> chats;

    private String SenderReciever,senderId,reciverId,ReciverSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        getSupportActionBar().hide();

        chats = new ArrayList<>();

        Intent intent = this.getIntent();
        reciverId = intent.getStringExtra("userKey");

        currUser = FirebaseAuth.getInstance().getCurrentUser();
        senderId = currUser.getUid();

        SenderReciever = senderId + reciverId ;
        ReciverSender = reciverId + senderId;

        database = FirebaseDatabase.getInstance();
        senderRef = database.getReference().child("Messages").child(SenderReciever);
        reciverRef = database.getReference().child("Messages").child(ReciverSender);

        TextView key = findViewById(R.id.userName);

        chatsRecycler = findViewById(R.id.chatRecycler);
        chatsRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mMessageAdapter = new MessageAdapter(this,this.getLayoutInflater());
        chatsRecycler.setAdapter(mMessageAdapter);

        message = findViewById(R.id.message);
        messageSend =findViewById(R.id.messageSend);

        senderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chats.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    UpMesssage msg = dataSnapshot.getValue(UpMesssage.class);
                    chats.add(msg);
                }
                mMessageAdapter.setChats(chats);
                chatsRecycler.scrollToPosition(chats.size() - 1);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        messageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                if (!msg.isEmpty()){

                    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    Date date = new Date();

                    UpMesssage upMesssage = new UpMesssage(senderId,msg,dateFormat.format(date));
                    Toast.makeText(getApplicationContext(),upMesssage.toString(),Toast.LENGTH_SHORT).show();

                    senderRef.push().setValue(upMesssage);
                    reciverRef.push().setValue(upMesssage);

                    message.getText().clear();
                }
            }
        });

    }
}