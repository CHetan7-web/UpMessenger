package com.example.upmessenger.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
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
import com.example.upmessenger.Adapters.UnReadMessageAdapter;
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
import com.google.firebase.database.ServerValue;
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

public class MessagesActivity extends AppCompatActivity implements OnNetworkGone, MessageHeaderItemDecoration.SectionCallback {

    FirebaseUser currUser;
    FirebaseDatabase database;
    DatabaseReference senderMsgRef, reciverMsgRef, recieverRef, senderRef, recieverUsers, senderUsers;
    DatabaseReference unreadSenderMsgRef, unreadReciverMsgRef;

    RecyclerView chatsRecycler, unreadChatsRecycler;
    MessageAdapter mMessageAdapter;
    UnReadMessageAdapter mUnreadMessageAdapter;

    BroadcastReceiver networkChangeReciever;

    EditText message;
    ImageView messageSend, profileImg, backImage;
    TextView profileName, userState, unreadMessage;

    LinearLayout unreadMessagesHeader;

    ArrayList<UpMesssage> chats, unreadChats;

    HashMap<String, Object> updateUser;

    Integer userStateCode = 0, userRecieverCode = 0;
    Long lastSeenTime;
    Boolean lastSeenFound;
    Boolean onResumed = true;
    Boolean lastSeenHeader = false;

    private String SenderReciever, senderId, reciverId, ReciverSender;
    private Date previousDate = new Date();
    private String prevHeader;
    private String lastMsgID;

    private long lastMessageSeenTime;
    private ValueEventListener seenListner, seenListnerMsg;
    private boolean onPaused;

    @Override
    protected void onResume() {
        super.onResume();

//        lastSeenTime = 0l;
        lastSeenFound = false;
        onPaused = false;

        registerReceiver(networkChangeReciever, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        //Sender status is reciver is Online
        userRecieverCode = 1;
        senderUsers.child("state").setValue(1);//updateChildren(updateUser);
        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 1");

        setUserOnline();

    }

    @Override
    public void setUserOnline() {
        //TYping , Online , OnaAPp
        recieverUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UpLastMessage upLastMessage = snapshot.getValue(UpLastMessage.class);

                if (upLastMessage.getMsgSenderId() != null)
                    lastMsgID = upLastMessage.getMsgSenderId();

                if (upLastMessage != null) {
                    Log.d("USER_STATE_CHANGED", String.valueOf(upLastMessage));
                    lastMessageSeenTime = upLastMessage.getLastMessageSeen();
                    if (upLastMessage.getState() != null) {
                        Log.d("USER_STATE_CHANGED", upLastMessage.getTyping().toString());

                        if (userStateCode == 1 && NetworkUtil.INSTANCE.getConnectivityStatus(getApplicationContext()) != 0) {
                            if (upLastMessage.getTyping() == 1) {
                                userState.setText("Typing.. .");
                            } else {
                                if (upLastMessage.getState() == 1) {
                                    userState.setText("onLine");
//                                    senderUsers.child("lastTime").addValueEventListener(seenListner);
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

        //Check for unread and then fetch all messasges
        senderUsers.child("lastMessageSeen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastSeen = snapshot.getValue(Long.class);
//                lastSeenTime = upLastMessage.getLastMessageSeen();
                lastMessageSeenTime = lastSeen;
                lastSeenFound = false;
                lastSeenTime = lastSeen;

                senderMsgRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int pos = -1;
                        chats.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            UpMesssage msg = dataSnapshot.getValue(UpMesssage.class);

                            Log.d("DAY_HEADER_MSG", "position " + chats.size() + " " + msg.getTime() + " > " + lastSeenTime + " " + (msg.getTime() > lastSeenTime) + " " + (msg.getUserId() == reciverId));
                            Log.d("DAY_HEADER_MSG", String.valueOf(lastSeenFound));

                            if (lastSeenFound == false && lastSeenTime != 0 && msg.getTime() > lastSeenTime && (lastMsgID.equals(reciverId))) {
                                pos = chats.size();
                                Log.d("DAY_HEADER_MSG", "position " + chats.size() + " Unread Message Found");
                                chats.add(new UpMesssage("UnreadMessage"));
                                lastSeenFound = true;
                            }
                            chats.add(msg);

                        }
//                        lastMessageSeenTime=chats.get(chats.size()-1).getTime();
                        lastSeenFound = false;
                        mMessageAdapter.setChats(chats);
                        if (lastSeenFound == true)
                            chatsRecycler.scrollToPosition(pos + 1);
                        else
                            chatsRecycler.scrollToPosition(chats.size() - 1);

                        Log.d("SEEN_MESSAGES", "onResumed == false " + (onResumed == false) + " onPaused == false " + (onPaused == false));
                        if (onResumed == false && onPaused == false) {
                            Log.d("SEEN_MESSAGES", "Through msgs added");
                            recieverUsers.child("lastTime").addValueEventListener(seenListner);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //For Seen in Reciver Acticity
        senderUsers.child("lastTime").addValueEventListener(seenListner);
        onResumed = false;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        getSupportActionBar().hide();

        updateUser = new HashMap<>();
        chats = new ArrayList<>();
        unreadChats = new ArrayList<>();

//        final Long[] lastSeenTime = {0l};
        final Boolean[] lastSeenFound = {false};
        Boolean lastSeenHeader = false;

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

        unreadSenderMsgRef = database.getReference().child("Unread").child(SenderReciever);
        unreadReciverMsgRef = database.getReference().child("Unread").child(ReciverSender);

        seenListner = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (onPaused == false) {
                    Long lastMsgTime = snapshot.getValue(Long.class);
                    Log.d("SEEN_MESSAGES", "lastMessage>lastMsgSeen " + (lastMsgTime > lastMessageSeenTime));

                    if (lastMsgTime > lastMessageSeenTime) {

                        senderMsgRef.orderByChild("time").startAfter(lastMessageSeenTime).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Log.d("SEEN_MESSAGES", "Got Unread Messages count : " + snapshot.getChildrenCount());
                                for (DataSnapshot snp : snapshot.getChildren()) {
                                    UpMesssage upMesssage = snp.getValue(UpMesssage.class);
                                    Log.d("SEEN_MESSAGES", upMesssage.getMessage());
                                    reciverMsgRef.orderByChild("time").equalTo(upMesssage.getTime()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot snp1 : snapshot.getChildren()) {
                                                Log.d("SEEN_MESSAGES", "Changing Seen State");
                                                snp1.getRef().child("seen").setValue(1);
                                            }
                                            snapshot.getRef().removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                                snapshot.getRef().removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    snapshot.getRef().removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        seenListnerMsg = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastMsgTime = snapshot.getValue(Long.class);
                Log.d("SEEN_MESSAGES", "lastMessage>lastMsgSeen " + (lastMsgTime > lastMessageSeenTime));

                if (lastMsgTime > lastMessageSeenTime) {

                    senderMsgRef.orderByChild("time").startAfter(lastMessageSeenTime).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d("SEEN_MESSAGES", "Got Unread Messages count : " + snapshot.getChildrenCount());
                            for (DataSnapshot snp : snapshot.getChildren()) {
                                UpMesssage upMesssage = snp.getValue(UpMesssage.class);
                                Log.d("SEEN_MESSAGES", upMesssage.getMessage());
                                reciverMsgRef.orderByChild("time").equalTo(upMesssage.getTime()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot snp1 : snapshot.getChildren()) {
                                            Log.d("SEEN_MESSAGES", "Changing Seen State");
                                            snp1.getRef().child("seen").setValue(1);
                                        }
                                        snapshot.getRef().removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            snapshot.getRef().removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        profileName = findViewById(R.id.profileName);
        userState = findViewById(R.id.userState);

        chatsRecycler = findViewById(R.id.chatRecycler);
        chatsRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mMessageAdapter = new MessageAdapter(this, this.getLayoutInflater());
        chatsRecycler.setAdapter(mMessageAdapter);

        MessageHeaderItemDecoration messageDayItemDecoration =
                new MessageHeaderItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height),
                        true,
                        this);

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

        Log.d("SERVER_VALUE", "Server Time is " + ServerValue.TIMESTAMP);

        //For User State , Profile,Name
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

        //lastSeenTimeEg = 1643635076198l;

        //After msg send is Pressed
        messageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                if (!msg.isEmpty()) {

                    updateUser.clear();

                    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    Date date = new Date();

                    UpMesssage upMesssage = new UpMesssage(senderId, msg, date.getTime(), 0);
                    Toast.makeText(getApplicationContext(), upMesssage.toString(), Toast.LENGTH_SHORT).show();

                    updateUser.put("lastMessage", msg);
                    updateUser.put("lastTime", date.getTime());
                    updateUser.put("msgSenderId", senderId);

//                    senderUsers.updateChildren(updateUser).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void unused) {
//                            senderMsgRef.push().setValue(upMesssage);
//                        }
//                    });
                    lastMsgID = senderId;

                    senderMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //We will not use it anymore because last message should reflect only with connected user not all
//                            senderRef.updateChildren(updateUser);
//                            senderUsers.child(reciverId).child("lastTime").setValue(updateUser.get("lastTime"));
                            updateUser.put("lastMessageSeen", updateUser.get("lastTime"));
                            senderUsers.updateChildren(updateUser);
                            updateUser.remove("lastMessageSeen");
                        }
                    });

//                    updateUser.remove("lastMessageSeen");

                    reciverMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //We will not use it anymore because last message should reflect only with connected user not all
//                            recieverRef.updateChildren(updateUser);
//                            recieverUsers.child(senderId).child("lastTime").setValue(updateUser.get("lastTime"));
//                            updateUser.put("seen",1);
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
    protected void onDestroy() {
        super.onDestroy();
        Log.d("SEEN_MESSAGES", "On Paused True");
        onPaused = true;
        onResumed = false;
        senderUsers.child("lastTime").removeEventListener(seenListner);
//        reciverMsgRef.removeEventListener();
//        unregisterReceiver(networkChangeReciever);
        //Sender status is reciver is Offline
        userRecieverCode = 0;
        senderUsers.child("state").setValue(0);
        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 0");

        senderUsers.child("lastMessageSeen").setValue(chats.get(chats.size() - 1).getTime());
    }

    @Override
    protected void onStop() {
        super.onStop();
        onPaused = true;
        onResumed = false;

        Log.d("SEEN_MESSAGES", "On Paused True");

        senderUsers.child("lastTime").removeEventListener(seenListner);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPaused = true;
        onResumed = false;

        Log.d("SEEN_MESSAGES", "On Paused True");

        senderUsers.child("lastTime").removeEventListener(seenListner);
        unregisterReceiver(networkChangeReciever);
        //Sender status is reciver is OnApp
        userRecieverCode = 0;
        senderUsers.child("state").setValue(0);
        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 0");

        senderUsers.child("lastMessageSeen").setValue(chats.get(chats.size() - 1).getTime());

    }

    @Override
    public void setUserStatusOffline() {
        userState.setText("You are Offline !!");
        Log.d("USER_STATE_UPDATE", "User State Set Offline");
    }

    @Override
    public boolean isSection(int position) {
//        Log.d("Day_Header_is_Section", String.valueOf(position));
        DateFormat dateFormat = new SimpleDateFormat("dd MMM , yyyy");

        if (chats.get(position).getUserId().equals("UnreadMessage"))
            return false;

//        if (lastSeenFound == false && 1643635076198l < lastSeenTime ){
//            chatsRecycler.scrollToPosition(position);
//            lastSeenFound = true;
//            lastSeenHeader = false;
//            return true;
//        }

        Date msgDate = new Date(dateFormat.format(chats.get(position).getTime()));
        //  Date prvDate = new Date(dateFormat.format(chats.get(position-1).getTime()));

        if (position == 0) {
            previousDate = msgDate;
            return true;
        }

        boolean result = msgDate.compareTo(previousDate) != 0;
        previousDate = msgDate;

        return result;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public CharSequence getSectionHeader(int position) {
        String header;//= new String();
        if (chats.get(position).getUserId().equals("UnreadMessage")) {
            Log.d("DAY_HEADER", position + " " + chats.get(position).getMessage());
            return prevHeader;

        }
        DateFormat dateFormat = new SimpleDateFormat("dd MMM , yyyy");

        Date msgDate = new Date(dateFormat.format(chats.get(position).getTime()));
        Date todaysDate = new Date(dateFormat.format((new Date()).getTime()));

        if (msgDate.compareTo(todaysDate) == 0)
            header = "Today";
        else if ((msgDate.getYear() == todaysDate.getYear()) && (msgDate.getMonth() == todaysDate.getMonth()) && (msgDate.getDate() + 1 == todaysDate.getDate()))
            header = "YesterDay";
        else
            header = dateFormat.format(msgDate);

        prevHeader = header;
        Log.d("DAY_HEADER", header + " Pos : " + position + " " + chats.get(position).getMessage());
        return header;
    }
}
