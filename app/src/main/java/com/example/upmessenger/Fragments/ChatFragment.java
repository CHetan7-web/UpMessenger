package com.example.upmessenger.Fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upmessenger.Activity.AddUserActivity;
import com.example.upmessenger.Activity.MessagesActivity;
import com.example.upmessenger.Adapters.UserAdapter;
import com.example.upmessenger.Models.UpUsers;
import com.example.upmessenger.R;
import com.example.upmessenger.UserOnClick;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChatFragment extends Fragment implements UserOnClick {

    FloatingActionButton addUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    RecyclerView userRecycler;
    UserAdapter mUserAdapter;
    ArrayList<String> users;

    public ChatFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        String userId;
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
             userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        else
            userId="";
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference()
                                    .child("Users-Connected")
                                    .child(userId);

        setUserState(userId);

        View view = inflater.inflate(R.layout.fragment_chat,container,false);

        addUser = view.findViewById(R.id.add_user);
        
        users = new ArrayList<>();

        userRecycler = view.findViewById(R.id.userRecycler);
        userRecycler.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        Drawable mDivider = ContextCompat.getDrawable(getActivity(), R.drawable.divider);

        DividerItemDecoration vItemDecoration = new DividerItemDecoration(userRecycler.getContext(),
                DividerItemDecoration.VERTICAL);
        // Set the drawable on it
        vItemDecoration.setDrawable(mDivider);
        userRecycler.addItemDecoration(vItemDecoration);

        mUserAdapter = new UserAdapter(inflater, this,inflater.getContext());
        userRecycler.setAdapter(mUserAdapter);

        Query ref = databaseReference.orderByChild("lastTime");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String text = "";
                    users.clear();
                    for (DataSnapshot child:snapshot.getChildren()){
                        if (!child.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            Log.d("CHILD_KEY", child.getKey());
                            users.add(0,child.getKey());
                        }
                    }
                  //  Collections.reverse(users);
                    mUserAdapter.setUsers(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addUserIntent = new Intent(getActivity(), AddUserActivity.class);
                startActivity(addUserIntent);
            }
        });

        return view;
    }

    @Override
    public void userClick(int postition) {

        Toast.makeText(getContext(),"User Clicked",Toast.LENGTH_SHORT).show();

        String key = users.get(postition);

        Intent chatIntent = new Intent(getActivity(), MessagesActivity.class);
        chatIntent.putExtra("userKey",key);

        startActivity(chatIntent);

    }

    public void setUserState(String userId){

        // Since I can connect from multiple devices, we store each connection instance separately
// any time that connectionsRef's value is null (i.e. has no children) I am offline
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myConnectionsRef = database.getReference("Users/"+userId).child("state");

// Stores the timestamp of my last disconnect (the last time I was seen online)
        final DatabaseReference lastOnlineRef = database.getReference("Users/"+userId).child("lastSeen");

        final DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            private static final String TAG = "USER_ONLINE_STATE";

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {

                    // When this device disconnects, change state
                    myConnectionsRef.onDisconnect().setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("USER_STATE_UPDATES","User State changed to 0 Offline");
                        }
                    });

                    // When I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                    // Add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    myConnectionsRef.setValue(1).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("USER_STATE_UPDATES","User State changed to 1");
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Listener was cancelled at .info/connected");
            }
        });
    }

}