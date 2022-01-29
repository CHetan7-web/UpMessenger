package com.example.upmessenger.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.upmessenger.Adapters.MessageAdapter;
import com.example.upmessenger.Models.UpMesssage;
import com.example.upmessenger.Models.UpUsers;
import com.example.upmessenger.R;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.HashMap;
import java.util.Map;

public class MessagesActivity extends AppCompatActivity {

    FirebaseUser currUser;
    FirebaseDatabase database;
    DatabaseReference senderMsgRef,reciverMsgRef,recieverRef,senderRef,recieverUsers,senderUsers;

    RecyclerView chatsRecycler ;
    MessageAdapter mMessageAdapter;

    EditText message;
    ImageView messageSend,profileImg,backImage;
    TextView profileName;

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

        senderMsgRef = database.getReference().child("Messages").child(SenderReciever);
        reciverMsgRef = database.getReference().child("Messages").child(ReciverSender);

        recieverRef = database.getReference().child("Users").child(reciverId);
        senderRef = database.getReference().child("Users").child(senderId);

        recieverUsers = database.getReference().child("Users-Connected").child(reciverId);
        senderUsers = database.getReference().child("Users-Connected").child(senderId);

        profileName = findViewById(R.id.profileName);

        chatsRecycler = findViewById(R.id.chatRecycler);
        chatsRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mMessageAdapter = new MessageAdapter(this,this.getLayoutInflater());
        chatsRecycler.setAdapter(mMessageAdapter);

        message = findViewById(R.id.message);
        messageSend =findViewById(R.id.messageSend);
        profileName = findViewById(R.id.profileName);
        profileImg = findViewById(R.id.profileImage);
        backImage = findViewById(R.id.backImage);

        if (intent.hasExtra("SHARED_MESSAGE")){
            message.setText(intent.getStringExtra("SHARED_MESSAGE").toString());
            message.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        backImage.setOnClickListener((view)->finish());

        recieverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UpUsers upUser = snapshot.getValue(UpUsers.class);
                if (upUser.getName() != null)
                    profileName.setText(upUser.getName());
                if (upUser.getProfilePic() != ""){
                    Glide.with(getApplicationContext())
                            .load(upUser.getProfilePic())
                            .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_foreground))
                            .into(profileImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        senderMsgRef.addValueEventListener(new ValueEventListener() {
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

                    HashMap<String,Object> updateUser = new HashMap<>();

                    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    Date date = new Date();

                    UpMesssage upMesssage = new UpMesssage(senderId,msg,date.getTime());
                    Toast.makeText(getApplicationContext(),upMesssage.toString(),Toast.LENGTH_SHORT).show();

                    updateUser.put("lastMessage",msg);
                    updateUser.put("lastTime",date.getTime());

                    senderMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            senderRef.updateChildren(updateUser);
                            senderUsers.child(reciverId).child("lastTime").setValue(updateUser.get("lastTime"));
                        }
                    });

                    reciverMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            recieverRef.updateChildren(updateUser);
                            recieverUsers.child(senderId).child("lastTime").setValue(updateUser.get("lastTime"));
                        }
                    });

                    message.getText().clear();
                }
            }
        });

    }

}