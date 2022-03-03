package com.example.upmessenger.Activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.upmessenger.Adapters.MessageAdapter;
import com.example.upmessenger.Adapters.UnReadMessageAdapter;
import com.example.upmessenger.Component.UpEditText;
import com.example.upmessenger.Extras.MessageHeaderItemDecoration;
import com.example.upmessenger.Models.UpLastMessage;
import com.example.upmessenger.Models.UpMesssage;
import com.example.upmessenger.Models.UpUsers;
import com.example.upmessenger.Networks.NetworkUtil;
import com.example.upmessenger.OnKeyboardVisibilityListener;
import com.example.upmessenger.OnNetworkGone;
import com.example.upmessenger.R;
import com.example.upmessenger.UpBroadcastReciever.NetworkChangeReciever;
import com.example.upmessenger.Utils.BlurBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kevalpatel2106.emoticongifkeyboard.EmoticonGIFKeyboardFragment;
import com.kevalpatel2106.emoticongifkeyboard.emoticons.Emoticon;
import com.kevalpatel2106.emoticongifkeyboard.emoticons.EmoticonSelectListener;
import com.kevalpatel2106.emoticongifkeyboard.gifs.Gif;
import com.kevalpatel2106.emoticongifkeyboard.gifs.GifSelectListener;
import com.kevalpatel2106.emoticongifkeyboard.stickers.Sticker;
import com.kevalpatel2106.emoticongifkeyboard.stickers.StickerPackLoader;
import com.kevalpatel2106.emoticongifkeyboard.stickers.StickerSelectListner;
import com.kevalpatel2106.emoticonpack.android7.Android7EmoticonProvider;
import com.kevalpatel2106.gifpack.giphy.GiphyGifProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class MessagesActivity extends AppCompatActivity implements OnNetworkGone, MessageHeaderItemDecoration.SectionCallback, OnKeyboardVisibilityListener {

    public static String IMG_PATH = "PICTURES/UPMESSAGE/MEDIA/UPMESSAGE IMAGES/";
    public static String GIF_PATH = "PICTURES/UPMESSAGE/MEDIA/ANIMATED GIFs/";
    public static String THUMB_PATH = "PICTURES/UPMESSAGE/MEDIA/THUMBNAILS/";
    public static String STICKER_PATH = "PICTURES/UPMESSAGE/MEDIA/STICKERS/";

    public static final String SERVER_URL = "http://stormbreaker107.pythonanywhere.com/singleMessage";
    public static final String[] MIME_TYPES = new String[]{"image/gif", "image/png", "image/jpg", "image/webp"};

    FirebaseUser currUser;
    FirebaseDatabase database;
    DatabaseReference senderMsgRef, reciverMsgRef, recieverRef, senderRef, recieverUsers, senderUsers;
    DatabaseReference unreadSenderMsgRef, unreadReciverMsgRef;

    StorageReference storageRef;

    RecyclerView chatsRecycler, unreadChatsRecycler;
    MessageAdapter mMessageAdapter;
    UnReadMessageAdapter mUnreadMessageAdapter;

    BroadcastReceiver networkChangeReciever;


    UpEditText message;
    ImageView messageSend;
    //    EmoticonEditText message;
    ImageView profileImg, backImage, emojiButton;
    TextView profileName, userState, unreadMessage;

    static Uri collection;

    static String[] PROJECTION;
    static String QUERY;

    LinearLayout unreadMessagesHeader;

    ArrayList<UpMesssage> chats, unreadChats;

    HashMap<String, Object> updateUser;

    Integer userStateCode = 0, userRecieverCode = 0;
    Integer RECIEVER_STATE = 0;

    Long lastSeenTime;
    Boolean lastSeenFound;
    Boolean onResumed = true;
    Boolean lastSeenHeader = false;
    Boolean isKeyboardShown = false;

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

    static ContentResolver mContentResolver;

    private EmoticonGIFKeyboardFragment mEmoticonGIFKeyboardFragment;
    InputMethodManager inputMethodManager;
    private final Integer[] drawble = {0};


    private boolean isReadPermissionGranted = false;
    private boolean isWritePermissionGranted = false;
    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private StorageReference recieverStorageReference;

    /**
     * Manually toggle soft keyboard visibility
     *
     * @param context calling context
     */
    public void toggleKeyboardVisibility(Context context) {
//        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            if (message != null)
                message.requestFocus();
        }
    }

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
//        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 1");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            message.setShowSoftInputOnFocus(true);
        }
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
//                        Log.d("USER_STATE_CHANGED", String.valueOf(upLastMessage));
                        if (upLastMessage.getLastMessageSeen() != null)
                            lastMessageSeenTime = upLastMessage.getLastMessageSeen();
                        if (upLastMessage.getState() != null) {
//                            Log.d("USER_STATE_CHANGED", upLastMessage.getTyping().toString());

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

                            } else {
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

        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {

                if (result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null) {
                    isReadPermissionGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                if (result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != null) {
                    isWritePermissionGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

            }
        });

        inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

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

        storageRef = FirebaseStorage.getInstance().getReference();
        recieverStorageReference = storageRef.child(senderId).child(reciverId);

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
//                                                    Log.d("SEEN_MESSAGES", snp1.getValue(UpMesssage.class).getMessage());
//                                                    Log.d("SEEN_MESSAGES", "Changing to Seen State");
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


        ////
        Uri MediaCollection = null;
        ContentResolver resolver = getContentResolver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            MediaCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        } else {

            MediaCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        collection = null;
        collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        PROJECTION = new String[]{MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.MediaColumns.RELATIVE_PATH};
        QUERY = MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? and " +
                MediaStore.Files.FileColumns.DISPLAY_NAME + " like ?";

        mContentResolver = this.getContentResolver();


//        File check = new File(getExternalFilesDir(null)+Environment.DIRECTORY_PICTURES+"/UPMESSAGE/PHOTOS/","final_image.jpg");
//        Log.d("EXTERNAL_STORAGE",getExternalFilesDir(null)+" "+Environment.DIRECTORY_PICTURES+check.exists());

//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "final_image1" + ".jpg");
//        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "PICTURES/UPMESSAGE/PHOTOS");
//
//        Uri imageUri = resolver.insert(collection, contentValues);
//
//        try {
//
//            OutputStream outputStream = resolver.openOutputStream(Objects.requireNonNull(imageUri));
//            @SuppressLint("ResourceType") InputStream is = getResources().openRawResource(R.drawable.chats_background);
////            OutputStream os = new FileOutputStream(mFolder);
//            byte[] data = new byte[is.available()];
//            is.read(data);
//            outputStream.write(data);
////            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//            is.close();
//            Objects.requireNonNull(outputStream);
//            Log.d("ExternalStorage", "-> uri=" + imageUri);
//
////            return true;
//
//        } catch (Exception e) {
//
//            Toast.makeText(this, "Image not saved: \n" + e, Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//
//        }


        message = (UpEditText) findViewById(R.id.message);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            message.setShowSoftInputOnFocus(false);
        }


        //Set emoticon configuration.
        EmoticonGIFKeyboardFragment.EmoticonConfig emoticonConfig = new EmoticonGIFKeyboardFragment.EmoticonConfig()
                .setEmoticonProvider(Android7EmoticonProvider.create())
                .setEmoticonSelectListener(new EmoticonSelectListener() {

                    @Override
                    public void emoticonSelected(Emoticon emoticon) {
                        //Do something with new emoticon.
//                        Log.d(TAG, "emoticonSelected: " + emoticon.getUnicode());
                        message.append(emoticon.getUnicode(),
                                message.getSelectionStart(),
                                message.getSelectionEnd());
                    }

                    @Override
                    public void onBackSpace() {
                        //Do something here to handle backspace event.
                        //The process of removing last character when user preses back space will handle
                        //by library if your edit text is in focus.
                    }
                });

        //Create GIF config
        EmoticonGIFKeyboardFragment.GIFConfig gifConfig = new EmoticonGIFKeyboardFragment
                .GIFConfig(GiphyGifProvider.create(this, "564ce7370bf347f2b7c0e4746593c179"))
                .setGifSelectListener(new GifSelectListener() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onGifSelected(@NonNull Gif gif) {
                        //Do something with the selected GIF.

                        String filename = getExtenstion(getFileName(gif.getGifUrl()), '/');

                        Log.d("UPMESSAGE_UPEDIT", "onGifSelected: " + gif.getGifUrl() + " filename : " + filename + " Preview Link " + gif.getPreviewGifUrl());


                        OkHttpClient client = new OkHttpClient();

                        Request request = null;

                        request = new Request.Builder()
                                .url(gif.getGifUrl())   //URL
                                .addHeader("Content-Type", "image/gif")
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Log.d("API_RESPONSE", e.toString());
                                e.getStackTrace();
                            }

                            @RequiresApi(api = Build.VERSION_CODES.Q)
                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    String contentType = response.header("Content-Type");
                                    Log.d("API_RESPONSE", contentType);

                                    ResponseBody responseBody = response.body();

                                    Log.d("API_RESPONSE", responseBody.byteStream().toString());

                                    storeMedia("gif", filename, null, getBytes(responseBody.byteStream()));
                                } else
                                    Log.d("API_RESPONSE", response.toString());
                            }

                        });


                    }
                });

        EmoticonGIFKeyboardFragment.StickerConfig stickerConfig = new EmoticonGIFKeyboardFragment.StickerConfig()
                .setStickerSelectListner(new StickerSelectListner() {

                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onStickerSelected(Sticker sticker, String identifier) {

                        Uri stickerUri = StickerPackLoader.getStickerAssetUri(identifier, sticker.getImageFileName());
                        InputStream stickerInputStream = null;

                        try {
                            stickerInputStream = getApplicationContext().getContentResolver().openInputStream(stickerUri);
                            storeMedia(getExtenstion(sticker.getImageFileName(), '.'), sticker.getImageFileName(), null, getBytes(stickerInputStream));

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Log.d("STICKER_FRAGMENT", "Sticker on Called " + sticker.getImageFileName() + " URI : " + StickerPackLoader.getStickerAssetUri(identifier, sticker.getImageFileName()));

                    }
                });


        mEmoticonGIFKeyboardFragment = EmoticonGIFKeyboardFragment
                .getNewInstance(findViewById(R.id.keyboard_container), emoticonConfig, gifConfig, stickerConfig);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.keyboard_container, mEmoticonGIFKeyboardFragment)
                .commit();


        drawble[0] = 0;
        emojiButton = findViewById(R.id.emoji_open_close_btn);
        emojiButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (drawble[0] == 0) {
                    if (isKeyboardShown)
                        toggleKeyboardVisibility(getApplicationContext());
                    emojiButton.setImageResource(R.drawable.ic_keyboard);
                    drawble[0] = 1;
                    mEmoticonGIFKeyboardFragment.toggle();
                    message.setShowSoftInputOnFocus(false);
                } else {
                    emojiButton.setImageResource(R.drawable.ic_emoticon);
                    drawble[0] = 0;
                    toggleKeyboardVisibility(MessagesActivity.this);
                    mEmoticonGIFKeyboardFragment.toggle();
                    message.setShowSoftInputOnFocus(true);

                }
//                Log.d("UPKEYBOARD", "" + inputMethodManager.isAcceptingText() + " : " + mEmoticonGIFKeyboardFragment.isOpen());

            }
        });


        requestPermission();

        //Left to Work on this
        message.setImgTypeString(MIME_TYPES);
        message.setKeyBoardInputCallbackListener(new UpEditText.KeyBoardInputCallbackListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onCommitContent(@Nullable InputContentInfoCompat inputContentInfo, int flags, @Nullable Bundle opts) {

                InputStream iStream = null;

                Uri contentUri = inputContentInfo.getContentUri();
                String type = getExtenstion(contentUri.toString(), '.');

                String filename;
                if (type.equals("gif"))
                    filename = getExtenstion(contentUri.toString(), '/');
                else
                    filename = "UP" + inputContentInfo.getLinkUri().toString().hashCode() + "." + type;

                Log.d("UPMESSAGE_UPEDIT", String.valueOf(inputContentInfo.getLinkUri().toString()));
                Log.d("UPMESSAGE_UPEDIT", "filename : " + filename + " type " + type + " URI " + inputContentInfo.getContentUri().toString());


                if (type.equals("gif")) {

                    try {
                        iStream = getContentResolver().openInputStream(contentUri);
                        byte[] inputData = getBytes(iStream);
                        storeMedia(type, filename, null, inputData);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        Bitmap bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), contentUri);
//                        Bitmap editedavatar = AndroidUtils.eraseColor(bitmap, -16777216);

                        storeMedia(type, filename, bm, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

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
//                    Log.d("USER_TYPING", "Recieved Data " + typingStatus);

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

//        Log.d("SERVER_VALUE", "Server Time is " + ServerValue.TIMESTAMP);

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
                            if (reciverDeviceToken != null && !reciverDeviceToken.equals("empty") && userRecieverCode == 0) {
                                //For Notification
                                sendMsgToServer(SERVER_URL, reciverDeviceToken);
                                //TO change status to 'Send'
                                senderMsgRef.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //Message Send
                                        for (DataSnapshot snp : snapshot.getChildren()) {
                                            snp.getRef().child("seen").setValue(3);
//                                            Log.d("SEEN_MESSAGES", "Changing state to Send");
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
                                    snapshot.getRef().setValue(count == null ? 0 : count + 1);

                                    snapshot.getRef().removeEventListener(this);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
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
//                Log.d("", "");
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
                                senderUsers.child("typing").setValue(0);//send notification for stopped typing event
                            }
                        },
                        DELAY
                );
            }
        });

        setKeyboardVisibilityListener(this);

    }

    private String getFileName(String path) {

        char ch = '/';
        String filename = "";

        int i = path.lastIndexOf(ch);
        int p = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));

        if (i >= p) {

            filename = path.substring(0, i) + "_" + path.substring(i + 1);

        }
        return filename;

    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String getExtenstion(String path, char ch) {
        String extension = "";

        int i = path.lastIndexOf(ch);
        int p = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));

        if (i >= p) {
            extension = path.substring(i + 1);
        }
        return extension;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void storeMedia(String type, String filename, @Nullable Bitmap originalBitmap, @Nullable byte[] bytes) {
        Log.d("EXTERNAL_STORAGE", "storeMedia: " + type + " " + filename);
        String thumbnail_name = "thumbnail-" + filename;

        Bitmap blurredBitmap = null;
        UploadTask upTask;

        ByteArrayOutputStream baos;

        updateUser.clear();

        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        Date date = new Date();

        updateUser.put("lastMessage", "Some Media");
        updateUser.put("lastTime", date.getTime());
        updateUser.put("msgSenderId", senderId);
        updateUser.put("thumbPath", thumbnail_name);
        updateUser.put("mediaPath", filename);


        if (type.equals("png") || type.equals("jpg") || type.equals("jpeg")) {

            blurredBitmap = BlurBuilder.blur(this, originalBitmap);

            //Check if thumbnail is present or not
            if (!mediaExists(THUMB_PATH, thumbnail_name)) {
                saveMedia(THUMB_PATH, thumbnail_name, blurredBitmap, null, "jpg");
            }

            //Check if original media is present or not
            if (!mediaExists(IMG_PATH, filename)) {
                //save image
                saveMedia(IMG_PATH, filename, originalBitmap, null, type);

            }

            //First add to Sender Msg

            //  senderId , time , sending , msg_type , thumbnail_name , media_name ,
            UpMesssage upMesssage = new UpMesssage(senderId, date.getTime(), 0, UpMesssage.MESSAGE_WITH_IMAGE, thumbnail_name, filename);
            Toast.makeText(getApplicationContext(), upMesssage.toString(), Toast.LENGTH_SHORT).show();

            updateUser.put("msgType", upMesssage.getMsgType());

            //Add new msg in users-reciever msg list
            senderMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    updateUser.put("lastMessageSeen", updateUser.get("lastTime"));
                    senderUsers.updateChildren(updateUser);
                    updateUser.remove("lastMessageSeen");

                }
            });

            DatabaseReference reciverMSGNode = reciverMsgRef.push();


            baos = new ByteArrayOutputStream();
            blurredBitmap.compress(Bitmap.CompressFormat.JPEG, 0, baos);
            byte[] thumbData = baos.toByteArray();

            //Then upload from sender
            recieverStorageReference.child(thumbnail_name).putBytes(thumbData)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            task.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d("UPMEDIA", " Thumburl " + uri.toString());
                                    upMesssage.setThumbUri(uri.toString());
                                    reciverMSGNode.setValue(upMesssage);

                                }
                            });
                        }
                    });


            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 0, baos1);
            byte[] imgData = baos1.toByteArray();


            upTask = recieverStorageReference.child(filename).putBytes(imgData);

            upTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    //Update media Progress
                    float progress = (float) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                    senderMsgRef.orderByChild("time").equalTo(upMesssage.getTime()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snp : snapshot.getChildren())
                                snp.getRef().child("mediaProgress").setValue(progress);

                            snapshot.getRef().removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            });

            upTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    task.getResult().getMetadata().getReference().getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    if (uri != null) {
                                        Log.d("UPMEDIA", " Image download on success " + uri.toString());
                                        Log.d("UPMEDIA", " Reciever Node " + reciverMSGNode.getRef().toString());
//                                        reciverMSGNode.child("mediaURL").setValue(uri.toString());
                                        upMesssage.setMediaUri(uri.toString());
                                        updateUser.put("mediaUri", uri.toString());
                                        reciverMSGNode.updateChildren(updateUser);
                                    }

                                }
                            })
                            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    //Add complete status in senders msg
                                    senderMsgRef.orderByChild("time").equalTo(upMesssage.getTime()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot snp : snapshot.getChildren())
                                                snp.getRef().child("mediaState").setValue(UpMesssage.MEDIA_DELIEVERD);

                                            snapshot.getRef().removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    //Done in Thumbnail part
                                    //add msg in reciever msg
//                                    reciverMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void unused) {
//
//                                            Log.d("UPMEDIA", " Image Uploaded ");
//
//                                            //Update unReadCount
//                                            recieverUsers.child("unReadCount").addListenerForSingleValueEvent(new ValueEventListener() {
//                                                @Override
//                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                                    Integer count = snapshot.getValue(Integer.class);
//                                                    snapshot.getRef().setValue(count == null ? 0 : count + 1);
//
//                                                    snapshot.getRef().removeEventListener(this);
//
//                                                }
//
//                                                @Override
//                                                public void onCancelled(@NonNull DatabaseError error) {
//                                                }
//                                            });
//                                            recieverUsers.updateChildren(updateUser);
//
//                                            //Notification for server
//                                            //if reciver device token is available and in not in user's message activity then send the notification
//                                            //If reciever is already in user's message activity , there is no need to send notification and status will be marked as 'Seen'
//                                            if (reciverDeviceToken != null && !reciverDeviceToken.equals("empty") && userRecieverCode == 0) {
//                                                //For Notification
//                                                sendMsgToServer(SERVER_URL, reciverDeviceToken);
//                                                //TO change status to 'Send'
//                                                //You can also use msgTime to get Ref
//                                                senderMsgRef.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
//                                                    @Override
//                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                                        //Message Send
//                                                        for (DataSnapshot snp : snapshot.getChildren()) {
//                                                            snp.getRef().child("seen").setValue(3);
////                                                    Log.d("SEEN_MESSAGES", "Changing state to Send");
//                                                        }
//                                                        snapshot.getRef().removeEventListener(this);
//                                                    }
//
//                                                    @Override
//                                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                                    }
//                                                });
//                                            }
//
//                                        }
//                                    });
//                                    reciverMsgRef.orderByChild("time").equalTo(upMesssage.getTime()).

                                }
                            });

                }
            });

            //After upload add to Reciver Msg
            //Done in complete listber

        } else if (type.equals("gif")) {

            //Check if thumbnail is present or not
            blurredBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            //Check if thumbnail is present or not
            if (!mediaExists(THUMB_PATH, thumbnail_name)) {
                saveMedia(THUMB_PATH, thumbnail_name, blurredBitmap, null, "png");
            }

            //Check if original media is present or not
            if (!mediaExists(GIF_PATH, filename)) {
                //save image
                saveMedia(GIF_PATH, filename, null, bytes, type);

            }

            //First add to Sender Msg

            //  senderId , time , sending , msg_type , thumbnail_name , media_name ,
            UpMesssage upMesssage = new UpMesssage(senderId, date.getTime(), 0, UpMesssage.MESSAGE_WITH_GIF, thumbnail_name, filename);
            //Toast.makeText(getApplicationContext(), upMesssage.toString(), Toast.LENGTH_SHORT).show();

            updateUser.put("msgType", upMesssage.getMsgType());

            //Add new msg in users-reciever msg list
            senderMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    updateUser.put("lastMessageSeen", updateUser.get("lastTime"));
                    senderUsers.updateChildren(updateUser);
                    updateUser.remove("lastMessageSeen");

                }
            });

            DatabaseReference reciverMSGNode = reciverMsgRef.push();

            baos = new ByteArrayOutputStream();
            blurredBitmap.compress(Bitmap.CompressFormat.JPEG, 0, baos);
            byte[] thumbData = baos.toByteArray();

            //Then upload from sender
            recieverStorageReference.child(thumbnail_name).putBytes(thumbData)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            task.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d("UPMEDIA", " Thumburl " + uri.toString());
                                    upMesssage.setThumbUri(uri.toString());
                                    reciverMSGNode.setValue(upMesssage);

                                }
                            });
                        }
                    });

            upTask = recieverStorageReference.child(filename).putBytes(bytes);

            upTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    //Update media Progress
                    float progress = (float) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                    senderMsgRef.orderByChild("time").equalTo(upMesssage.getTime())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot snp : snapshot.getChildren())
                                        snp.getRef().child("mediaProgress").setValue(progress);

                                    snapshot.getRef().removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }
            });

            upTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    task.getResult().getMetadata().getReference().getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    if (uri != null) {
                                        Log.d("UPMEDIA", " GIF upload on success " + uri.toString());
                                        Log.d("UPMEDIA", " Reciever Node " + reciverMSGNode.getRef().toString());
//                                        reciverMSGNode.child("mediaURL").setValue(uri.toString());
                                        upMesssage.setMediaUri(uri.toString());
                                        updateUser.put("mediaUri", uri.toString());
                                        reciverMSGNode.updateChildren(updateUser);
                                    }

                                }
                            })
                            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    //Add complete status in senders msg
                                    senderMsgRef.orderByChild("time").equalTo(upMesssage.getTime()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot snp : snapshot.getChildren())
                                                snp.getRef().child("mediaState").setValue(UpMesssage.MEDIA_DELIEVERD);

                                            snapshot.getRef().removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    //Done in Thumbnail part
                                    //add msg in reciever msg
//                                    reciverMsgRef.push().setValue(upMesssage).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void unused) {
//
//                                            Log.d("UPMEDIA", " Image Uploaded ");
//
//                                            //Update unReadCount
//                                            recieverUsers.child("unReadCount").addListenerForSingleValueEvent(new ValueEventListener() {
//                                                @Override
//                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                                    Integer count = snapshot.getValue(Integer.class);
//                                                    snapshot.getRef().setValue(count == null ? 0 : count + 1);
//
//                                                    snapshot.getRef().removeEventListener(this);
//
//                                                }
//
//                                                @Override
//                                                public void onCancelled(@NonNull DatabaseError error) {
//                                                }
//                                            });
//                                            recieverUsers.updateChildren(updateUser);
//
//                                            //Notification for server
//                                            //if reciver device token is available and in not in user's message activity then send the notification
//                                            //If reciever is already in user's message activity , there is no need to send notification and status will be marked as 'Seen'
//                                            if (reciverDeviceToken != null && !reciverDeviceToken.equals("empty") && userRecieverCode == 0) {
//                                                //For Notification
//                                                sendMsgToServer(SERVER_URL, reciverDeviceToken);
//                                                //TO change status to 'Send'
//                                                //You can also use msgTime to get Ref
//                                                senderMsgRef.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
//                                                    @Override
//                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                                        //Message Send
//                                                        for (DataSnapshot snp : snapshot.getChildren()) {
//                                                            snp.getRef().child("seen").setValue(3);
////                                                    Log.d("SEEN_MESSAGES", "Changing state to Send");
//                                                        }
//                                                        snapshot.getRef().removeEventListener(this);
//                                                    }
//
//                                                    @Override
//                                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                                    }
//                                                });
//                                            }
//
//                                        }
//                                    });
//                                    reciverMsgRef.orderByChild("time").equalTo(upMesssage.getTime()).

                                }
                            });

                }
            });

        } else if (type.equals("webp")) {
            //Check if original media is present or not
            if (!mediaExists(STICKER_PATH, filename)) {
                //save image
                saveMedia(STICKER_PATH, filename, null, bytes, type);

            }


        }

    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void saveMedia(String path, String name, @Nullable Bitmap bm, @Nullable byte[] bytes, String type) {

        Log.d("EXTERNAL_STORAGE", " on saveMedia: " + path + " " + name);

        //"image/jpeg"
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, type);
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, path);
        Uri contentUri = mContentResolver.insert(collection, contentValues);


        try {
            OutputStream fos = mContentResolver.openOutputStream(Objects.requireNonNull(contentUri));
            OutputStream fos1 = mContentResolver.openOutputStream(Objects.requireNonNull(contentUri));

            Log.d("EXTERNAL_STORAGE", type);
            if (type.equals("png") || type.equals("jpg") || type.equals("jpeg")) {
                assert bm != null;
                bm.compress(Bitmap.CompressFormat.PNG, 0, fos);
                Objects.requireNonNull(fos).close();
            } else {
                fos.write(bytes);
                Objects.requireNonNull(fos).close();
            }


            Log.d("ExternalStorage", "-> uri=" + contentUri);

        } catch (Exception e) {

//            Toast.makeText(MessagesActivity.this, "Image not saved: ", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }
    }

    public static boolean mediaExists(String path, String filename) {
        Cursor cursor = mContentResolver.query(collection, PROJECTION, QUERY, new String[]{"%" + path + "%", "%" + filename + "%"}, null);

        if (cursor != null) {
//            Log.d("upisdk", "cursor != null");
            if (cursor.getCount() > 0) {
                Log.d("EXTERNAL_STORAGE", "File exists");
                return true;
            } else {
                Log.d("EXTERNAL_STORAGE", "File doesn't exists " + filename + " path " + path);
                return false;
            }
        }
        return false;

    }

    private void requestPermission() {


        boolean minSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

        isReadPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;

        isWritePermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;

        isWritePermissionGranted = isWritePermissionGranted || minSDK;

        List<String> permissionRequest = new ArrayList<String>();

        if (!isReadPermissionGranted) {

            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        }
        if (!isWritePermissionGranted) {

            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionRequest.isEmpty()) {

            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }

    }


    @Override
    public void onBackPressed() {

//        Log.d("ON_BACK_UPKEYBOARD ", "Hi");
        if (mEmoticonGIFKeyboardFragment.isOpen()) {
            mEmoticonGIFKeyboardFragment.toggle();
            if (message != null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    message.clearFocus();
                    drawble[0] = 0;
                    emojiButton.setImageResource(R.drawable.ic_emoticon);
                    message.setShowSoftInputOnFocus(true);
                }
        } else
            super.onBackPressed();

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
//        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 0");

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
//        Log.d("USER_STATE_UPDATE", "User State Changed on Reciever : 0");

        senderUsers.child("unReadCount").setValue(0);

        if (chats.size() != 0)
            senderUsers.child("lastMessageSeen").setValue(chats.get(chats.size() - 1).getTime());

    }

    private void setKeyboardVisibilityListener(final OnKeyboardVisibilityListener onKeyboardVisibilityListener) {
        final View parentView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private boolean alreadyOpen;
            private final int defaultKeyboardHeightDP = 100;
            private final int EstimatedKeyboardDP = defaultKeyboardHeightDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
            private final Rect rect = new Rect();

            @Override
            public void onGlobalLayout() {
                int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, parentView.getResources().getDisplayMetrics());
                parentView.getWindowVisibleDisplayFrame(rect);
                int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
                boolean isShown = heightDiff >= estimatedKeyboardHeight;

                if (isShown == alreadyOpen) {
//                    Log.i("Keyboard state", "Ignoring global layout change...");
                    return;
                }
                alreadyOpen = isShown;
                onKeyboardVisibilityListener.onVisibilityChanged(isShown);
            }
        });
    }

    @Override
    public void setUserStatusOffline() {
        userState.setText("You are Offline !!");
//        Log.d("USER_STATE_UPDATE", "User State Set Offline");
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
//        Log.d("SECTION_NAME_HEADER", "Position : " + position + " Msg " + chats.get(position).getMessage());
        String header;//= new String();
        msgDate = new Date(dateFormat.format(chats.get(position).getTime()));

        if (chats.get(position).getUserId().equals("UnreadMessage")) {
            if (position == 0)
                msgDate = new Date(dateFormat.format(chats.get(position + 1).getTime()));
            else
                msgDate = new Date(dateFormat.format(chats.get(position - 1).getTime()));
//            Log.d("SECTION_NAME", position + " " + chats.get(position).getMessage());
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
//          Log.d("SECTION_NAME", header + " Pos : " + position + " " + chats.get(position).getMessage());
        return header;
    }

    @Override
    public boolean isSection(int position) {
//        Log.d("Day_Header_is_Section", String.valueOf(position));
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

    private void sendMsgToServer(String url, String deviceToken) {
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
                Log.d("API_RESPONSE", e.toString());
                e.getStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    Log.e("API_RESPONSE", responseBody.string());
                } else
                    Log.e("API_RESPONSE", response.toString());
            }

        });
    }

    @Override
    public void onVisibilityChanged(boolean visible) {

    }

}