package com.example.upmessenger.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


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

import org.chromium.net.HttpUtil;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessagesActivity extends AppCompatActivity implements OnNetworkGone, MessageHeaderItemDecoration.SectionCallback {

    FirebaseUser currUser;
    FirebaseDatabase database;
    DatabaseReference senderMsgRef, reciverMsgRef, recieverRef, senderRef, recieverUsers, senderUsers;
    DatabaseReference unreadSenderMsgRef, unreadReciverMsgRef;

    RecyclerView chatsRecycler, unreadChatsRecycler;
    MessageAdapter mMessageAdapter;
    UnReadMessageAdapter mUnreadMessageAdapter;

    BroadcastReceiver networkChangeReciever;

    public static final String SERVER_URL = "http://stormbreaker107.pythonanywhere.com/singleMessage";

    EditText message;
    ImageView messageSend, profileImg, backImage;
    TextView profileName, userState, unreadMessage;

    LinearLayout unreadMessagesHeader;

    ArrayList<UpMesssage> chats, unreadChats;

    HashMap<String, Object> updateUser;

    Integer userStateCode = 0, userRecieverCode = 0;
    Integer RECIEVER_STATE = 0;

    Long lastSeenTime;
    Boolean lastSeenFound;
    Boolean onResumed = true;
    Boolean lastSeenHeader = false;

    private String SenderReciever, senderId, reciverId, ReciverSender;
    private Date previousDate;//= new Date();
    private String prevHeader;
    private String lastMsgID;

    private long lastMessageSeenTime;
    private ValueEventListener seenListner, seenListnerMsg;
    private boolean onPaused;
    private String previousMsg;
    private SimpleDateFormat dateFormat;
    private String reciverDeviceToken;

    @Override
    protected void onResume() {
        super.onResume();

//        lastSeenTime = 0l;
        lastSeenFound = false;
        onPaused = false;
        //RegisterBroadCast Reciever for internet connection actions
        registerReceiver(networkChangeReciever, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        //Sender status in reciver is Online
        userRecieverCode = 1;
        senderUsers.child("state").setValue(1);//updateChildren(updateUser);
        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 1");

        setUserOnline();

    }

    @Override
    public void setUserOnline() {
        //TYping , Online , OnaAPp
        //Get Reciever Status
        recieverUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UpLastMessage upLastMessage = snapshot.getValue(UpLastMessage.class);
                if (upLastMessage != null) {

                    if (upLastMessage.getMsgSenderId() != null)
                        lastMsgID = upLastMessage.getMsgSenderId();

                    if (upLastMessage != null) {
                        Log.d("USER_STATE_CHANGED", String.valueOf(upLastMessage));
                        if (upLastMessage.getLastMessageSeen() != null)
                            lastMessageSeenTime = upLastMessage.getLastMessageSeen();
                        if (upLastMessage.getState() != null) {
                            Log.d("USER_STATE_CHANGED", upLastMessage.getTyping().toString());

                            //Set Revciever status appropriately if Internet connection is on
                            if (userStateCode == 1 && NetworkUtil.INSTANCE.getConnectivityStatus(getApplicationContext()) != 0) {

                                if (upLastMessage.getTyping() == 1) {
                                    userState.setText("Typing.. .");
                                    userRecieverCode = 1;
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

                            } else{
                                userState.setText("");
                                userRecieverCode = 0;
                            }

                        }
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
                if (lastSeen != null) {
                    lastMessageSeenTime = lastSeen;
                    lastSeenFound = false;
                    lastSeenTime = lastSeen;

                    //Fetch all msgs or ( new msgs )* and if there are unread msg,add a unreader header
                    senderMsgRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int pos = -1;
                            chats.clear();
                            //Add all msgs in Array List
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                UpMesssage msg = dataSnapshot.getValue(UpMesssage.class);

//                                Log.d("DAY_HEADER_MSG", "position " + chats.size() + " " + msg.getTime() + " > " + lastSeenTime + " " + (msg.getTime() > lastSeenTime) + " " + (msg.getUserId() == reciverId));
//                                Log.d("DAY_HEADER_MSG", String.valueOf(lastSeenFound));

                                if (lastSeenFound == false && lastSeenTime != 0 && msg.getTime() > lastSeenTime && (lastMsgID.equals(reciverId))) {
                                    pos = chats.size();
//                                    Log.d("DAY_HEADER_MSG", "position " + chats.size() + " Unread Message Found");
                                    chats.add(new UpMesssage("UnreadMessage"));
                                    lastSeenFound = true;
                                }
                                chats.add(msg);

                            }
//                        lastMessageSeenTime=chats.get(chats.size()-1).getTime();
                            lastSeenFound = false;
                            //For Header
                            if (chats.size() != 0)
                                previousDate = new Date(dateFormat.format(chats.get(0).getTime()));

                            //Bind ArrayList with Adapter
                            mMessageAdapter.setChats(chats);

                            //Scroll to appropriate position
                            if (lastSeenFound == true)
                                chatsRecycler.scrollToPosition(pos + 1);
                            else
                                chatsRecycler.scrollToPosition(chats.size() - 1);

//                            Log.d("SEEN_MESSAGES", "onResumed == false " + (onResumed == false) + " onPaused == false " + (onPaused == false));
                            //If there are new msg ,mark them also as seen
                            if (onResumed == false && onPaused == false) {
//                                Log.d("SEEN_MESSAGES", "Through msgs added");
                                recieverUsers.child("lastTime").addValueEventListener(seenListner);
                            }

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
                //Message can only be seen if MessageActivity is Visible
                if (onPaused == false) {
                    Long lastMsgTime = snapshot.getValue(Long.class);
                    if (lastMsgTime != null) {
//                        Log.d("SEEN_MESSAGES", "lastMessage>lastMsgSeen " + (lastMsgTime > lastMessageSeenTime));

                        if (lastMsgTime > lastMessageSeenTime) {
                            //Get Unread Messages and Mark as Seen
                            senderMsgRef.orderByChild("time").startAfter(lastMessageSeenTime).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    Log.d("SEEN_MESSAGES", "Got Unread Messages count : " + snapshot.getChildrenCount());
                                    //For every unread Msg , mark as 'SEEN' in reciever-user msg list
                                    for (DataSnapshot snp : snapshot.getChildren()) {
                                        UpMesssage upMesssage = snp.getValue(UpMesssage.class);
//                                        Log.d("SEEN_MESSAGES", upMesssage.getMessage());
                                        //Marking as Seen
                                        reciverMsgRef.orderByChild("time").equalTo(upMesssage.getTime()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot snp1 : snapshot.getChildren()) {
                                                    Log.d("SEEN_MESSAGES",snp1.getValue(UpMesssage.class).getMessage());
                                                    Log.d("SEEN_MESSAGES", "Changing to Seen State");
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

        dateFormat = new SimpleDateFormat("dd MMM , yyyy");

        backImage.setOnClickListener((view) -> finish());

        //For Typing Status
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

        //For User State , Profile,Name,Device Token
        recieverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UpUsers upUser = snapshot.getValue(UpUsers.class);
                //User Name in Top
                if (upUser.getName() != null)
                    profileName.setText(upUser.getName());

                //Profile Pic in Top
                if (upUser.getProfilePic() != "") {
                    Glide.with(getApplicationContext())
                            .load(upUser.getProfilePic())
                            .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_foreground))
                            .into(profileImg);
                }

                //Reciever State i
                if (upUser.getState() != null) {
                    //Offline
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

                //Device Token for FCM
                if (upUser.getDeviceToken() != null)
                    reciverDeviceToken = upUser.getDeviceToken();

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
                //get msg from editText
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

                    lastMsgID = senderId;

                    //Add new msg in users-reciever msg list
                    senderMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //We will not use it anymore because last message should reflect only with connected user not all
//                            senderRef.updateChildren(updateUser);
//                            senderUsers.child(reciverId).child("lastTime").setValue(updateUser.get("lastTime"));
                            updateUser.put("lastMessageSeen", updateUser.get("lastTime"));
                            senderUsers.updateChildren(updateUser);
                            updateUser.remove("lastMessageSeen");

                            //if reciver device token is available and in not in user's message activity then send the notification
                            //If reciever is already in user's message activity , there is no need to send notification and status will be marked as 'Seen'
                            if (reciverDeviceToken != null && !reciverDeviceToken.equals("empty") && userRecieverCode==0){
                                //For Notification
                                sendMsgToServer(SERVER_URL,reciverDeviceToken);
                                //TO change status to 'Send'
                                senderMsgRef.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //Message Send
                                        for (DataSnapshot snp:snapshot.getChildren()) {
                                            snp.getRef().child("seen").setValue(3);
                                            Log.d("SEEN_MESSAGES","Changing state to Send");
                                        }
                                        snapshot.getRef().removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }


                        }
                    });

//                    updateUser.remove("lastMessageSeen");
                    //Add new msg in reciever-user msg list and update unReadCount
                    reciverMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //We will not use it anymore because last message should reflect only with connected user not all
//                            recieverRef.updateChildren(updateUser);
//                            recieverUsers.child(senderId).child("lastTime").setValue(updateUser.get("lastTime"));
//                            updateUser.put("seen",1);

                            //Update unReadCount
                            recieverUsers.child("unReadCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Integer count = snapshot.getValue(Integer.class);
                                    snapshot.getRef().setValue(count==null?0:count + 1);

                                    snapshot.getRef().removeEventListener(this);

                                    }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
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
//        Log.d("SEEN_MESSAGES", "On Paused True");
        onPaused = true;
        onResumed = false;

        senderUsers.child("lastTime").removeEventListener(seenListner);

        //Sender status in reciver Message Acticity is Offline
        userRecieverCode = 0;
        senderUsers.child("state").setValue(0);
        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 0");

        senderUsers.child("unReadCount").setValue(0);
        senderUsers.child("lastMessageSeen").setValue(chats.get(chats.size() - 1).getTime());
    }

    @Override
    protected void onStop() {
        super.onStop();
        onPaused = true;
        onResumed = false;
        userRecieverCode = 0;

//        Log.d("SEEN_MESSAGES", "On Paused True");

        senderUsers.child("lastTime").removeEventListener(seenListner);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPaused = true;
        onResumed = false;

//        Log.d("SEEN_MESSAGES", "On Paused True");

        senderUsers.child("lastTime").removeEventListener(seenListner);
        unregisterReceiver(networkChangeReciever);
        //Sender status is reciver is OnApp
        userRecieverCode = 0;
        senderUsers.child("state").setValue(0);
        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 0");

        senderUsers.child("unReadCount").setValue(0);

        if (chats.size() != 0)
            senderUsers.child("lastMessageSeen").setValue(chats.get(chats.size() - 1).getTime());

    }

    @Override
    public void setUserStatusOffline() {
        userState.setText("You are Offline !!");
        Log.d("USER_STATE_UPDATE", "User State Set Offline");
    }

    private Date getPreviousDate(int position) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM , yyyy");
        if (position == 0)
            return new Date(dateFormat.format(chats.get(position).getTime()));
        if (chats.get(position - 1).getUserId().equals("UnreadMessage"))
            return getPreviousDate(position - 1);

        return new Date(dateFormat.format(chats.get(position - 1).getTime()));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public CharSequence getSectionHeader(int position) {
        Date msgDate;
        if (position == chats.size())
            return prevHeader;
        Log.d("SECTION_NAME_HEADER", "Position : " + position + " Msg " + chats.get(position).getMessage());
        String header;//= new String();
        msgDate = new Date(dateFormat.format(chats.get(position).getTime()));

        if (chats.get(position).getUserId().equals("UnreadMessage")) {
            if (position == 0)
                msgDate = new Date(dateFormat.format(chats.get(position + 1).getTime()));
            else
                msgDate = new Date(dateFormat.format(chats.get(position - 1).getTime()));
            Log.d("SECTION_NAME", position + " " + chats.get(position).getMessage());
//            return prevHeader;
        }
//        DateFormat dateFormat = new SimpleDateFormat("dd MMM , yyyy");

        Date todaysDate = new Date(dateFormat.format((new Date()).getTime()));

        if (msgDate.compareTo(todaysDate) == 0)
            header = "Today";
        else if ((msgDate.getYear() == todaysDate.getYear()) && (msgDate.getMonth() == todaysDate.getMonth()) && (msgDate.getDate() + 1 == todaysDate.getDate()))
            header = "YesterDay";
        else
            header = dateFormat.format(msgDate);

        prevHeader = header;
        //  Log.d("SECTION_NAME", header + " Pos : " + position + " " + chats.get(position).getMessage());
        return header;
    }

    @Override
    public boolean isSection(int position) {
        Log.d("Day_Header_is_Section", String.valueOf(position));
        DateFormat dateFormat = new SimpleDateFormat("dd MMM ,yyyy");

        Date msgDate = new Date(dateFormat.format(chats.get(position).getTime()));

        if (chats.get(position).getUserId().equals("UnreadMessage"))
            return false;

        if (position == 0)
            return true;

        Date prvDate = getPreviousDate(position);

        boolean result = msgDate.compareTo(prvDate) != 0;
        return result;

    }

    private void sendMsgToServer(String url,String deviceToken){
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("device_token", deviceToken)
                .add("key2", "value2")  //optional
//                .add("key3", "value3")  //optional
//                .add("key4", "value4")  //optional
                .build();

        Request request = null;

        try {
            request = new Request.Builder()
                    .url(url)   //URL
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(
                            MediaType.parse("application/json; charset=utf-8"),
                            new JSONObject().put("device_token", deviceToken).toString()))
                    .build();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("API_RESPONSE",e.toString());
                e.getStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    ResponseBody responseBody = response.body();
                    Log.e("API_RESPONSE", responseBody.string());
                }else
                    Log.e("API_RESPONSE", response.toString());
            }

    });
}

}
