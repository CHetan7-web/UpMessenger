package com.example.upmessenger.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.upmessenger.Adapters.MessageAdapter;
import com.example.upmessenger.Extras.MessageHeaderItemDecoration;
import com.example.upmessenger.Models.UpLastMessage;
import com.example.upmessenger.Models.UpMesssage;
import com.example.upmessenger.Models.UpUsers;
import com.example.upmessenger.Networks.NetworkUtil;
import com.example.upmessenger.OnNetworkGone;
import com.example.upmessenger.R;
import com.example.upmessenger.UpBroadcastReciever.NetworkChangeReciever;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MessagesActivity extends AppCompatActivity implements OnNetworkGone {

    FirebaseUser currUser;
    FirebaseDatabase database;
    DatabaseReference senderMsgRef, reciverMsgRef, recieverRef, senderRef, recieverUsers, senderUsers;

    RecyclerView chatsRecycler;
    MessageAdapter mMessageAdapter;

    BroadcastReceiver networkChangeReciever;

    EditText message;
    ImageView messageSend, profileImg, backImage;
    TextView profileName, userState;

    ArrayList<UpMesssage> chats;

    HashMap<String, Object> updateUser;

    Integer userStateCode = 0, userRecieverCode = 0;

    private String SenderReciever, senderId, reciverId, ReciverSender;

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(networkChangeReciever, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        userRecieverCode = 1;
        senderUsers.child("state").setValue(1);//updateChildren(updateUser);
        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 1");

        setUserOnline();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        getSupportActionBar().hide();

        updateUser = new HashMap<>();
        chats = new ArrayList<>();

        Intent intent = this.getIntent();
        reciverId = intent.getStringExtra("userKey");

        networkChangeReciever = new NetworkChangeReciever();

        currUser = FirebaseAuth.getInstance().getCurrentUser();
        senderId = currUser.getUid();

        SenderReciever = senderId + reciverId;
        ReciverSender = reciverId + senderId;

        database = FirebaseDatabase.getInstance();

        senderMsgRef = database.getReference().child("Messages").child(SenderReciever);
        reciverMsgRef = database.getReference().child("Messages").child(ReciverSender);
        recieverRef = database.getReference().child("Users").child(reciverId);
        senderRef = database.getReference().child("Users").child(senderId);
        recieverUsers = database.getReference().child("Users-Connected").child(reciverId).child(senderId);
        senderUsers = database.getReference().child("Users-Connected").child(senderId).child(reciverId);

        profileName = findViewById(R.id.profileName);
        userState = findViewById(R.id.userState);

        chatsRecycler = findViewById(R.id.chatRecycler);
        chatsRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mMessageAdapter = new MessageAdapter(this, this.getLayoutInflater());
        chatsRecycler.setAdapter(mMessageAdapter);

        MessageHeaderItemDecoration messageDayItemDecoration =
                new MessageHeaderItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height),
                        true,
                        mMessageAdapter);

        chatsRecycler.addItemDecoration(messageDayItemDecoration);

        message = findViewById(R.id.message);
        messageSend = findViewById(R.id.messageSend);
        profileName = findViewById(R.id.profileName);
        profileImg = findViewById(R.id.profileImage);
        backImage = findViewById(R.id.backImage);

        backImage.setOnClickListener((view) -> finish());

        recieverUsers.child("Typing").addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceType")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer typingStatus = snapshot.getValue(Integer.class);

                if (typingStatus != null) {
                    Log.d("USER_TYPING", "Recieved Data " + typingStatus);

                    if (typingStatus == 1) {
                        userState.setText("Typing..");
                        userState.setTextColor(getColor(R.color.green));
                    } else {
//                        userState.setTextColor(getColor(R.id.tabMode));
                        userState.setText("");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recieverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UpUsers upUser = snapshot.getValue(UpUsers.class);
                if (upUser.getName() != null)
                    profileName.setText(upUser.getName());
                if (upUser.getProfilePic() != "") {
                    Glide.with(getApplicationContext())
                            .load(upUser.getProfilePic())
                            .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_foreground))
                            .into(profileImg);
                }

                if (upUser.getState() != null) {
                    if (upUser.getState() == 0) {
                        userStateCode = 0;
                        userState.setText("Offline");
                    } else {
                        if (userRecieverCode == 1)
                            userState.setText("OnLine");
                        else
                            userState.setText("OnApp");

                        userStateCode = 1;
                    }
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
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
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
                if (!msg.isEmpty()) {

                    updateUser.clear();

                    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    Date date = new Date();

                    UpMesssage upMesssage = new UpMesssage(senderId, msg, date.getTime());
                    Toast.makeText(getApplicationContext(), upMesssage.toString(), Toast.LENGTH_SHORT).show();

                    updateUser.put("lastMessage", msg);
                    updateUser.put("lastTime", date.getTime());

                    senderMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //We will not use it anymore because last message should reflect only with connected user not all
//                            senderRef.updateChildren(updateUser);
//                            senderUsers.child(reciverId).child("lastTime").setValue(updateUser.get("lastTime"));
                            senderUsers.updateChildren(updateUser);
                        }
                    });

                    reciverMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //We will not use it anymore because last message should reflect only with connected user not all
//                            recieverRef.updateChildren(updateUser);
//                            recieverUsers.child(senderId).child("lastTime").setValue(updateUser.get("lastTime"));
                            recieverUsers.updateChildren(updateUser);
                        }
                    });

                    message.getText().clear();
                }
            }
        });

        message.addTextChangedListener(new TextWatcher() {

            boolean isTyping = false;
            String TAG = "USER_TYPING_STATUS";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            private Timer timer = new Timer();
            private final long DELAY = 2000; // milliseconds

            @Override
            public void afterTextChanged(final Editable s) {
                Log.d("", "");
                if (!isTyping) {
                    senderUsers.child("typing").setValue(1);                    // Send notification for start typing event
                    isTyping = true;
                }
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                isTyping = false;
                                senderUsers.child("typing").setValue(0);                                //send notification for stopped typing event
                            }
                        },
                        DELAY
                );
            }
        });

    }

    @Override
    public void setUserOnline() {
        recieverUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d("USER_STATE_CHANGED", String.valueOf(snapshot.getChildrenCount()));
//                for (DataSnapshot snch : snapshot.getChildren()) {
//                    Log.d("USER_STATE_CHANGED", snch.getKey() + " : " + snch.getValue());//+snch.getValue(String.class));
//                }

                UpLastMessage upLastMessage = snapshot.getValue(UpLastMessage.class);
                if (upLastMessage != null) {
                    Log.d("USER_STATE_CHANGED", String.valueOf(upLastMessage));
                    if (upLastMessage.getState() != null) {
                        Log.d("USER_STATE_CHANGED", upLastMessage.getTyping().toString());

                        if (userStateCode == 1 && NetworkUtil.INSTANCE.getConnectivityStatus(getApplicationContext()) != 0) {
                            if (upLastMessage.getTyping() == 1) {
                                userState.setText("Typing.. .");
                            } else {
                                if (upLastMessage.getState() == 1) {
                                    userState.setText("onLine");
                                    userRecieverCode = 1;
                                } else {
                                    userRecieverCode = 0;
                                    userState.setText("onApp");
                                }
                            }

                        } else
                            userState.setText("");

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReciever);
        userRecieverCode = 0;
        senderUsers.child("state").setValue(0);
        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 0");

    }


    @Override
    public void setUserStatusOffline() {
        userState.setText("You are Offline !!");
        Log.d("USER_STATE_UPDATE", "User State Set Offline");

    }


}