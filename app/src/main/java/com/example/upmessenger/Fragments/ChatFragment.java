package com.example.upmessenger.Fragments;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upmessenger.Adapters.UserAdapter;
import com.example.upmessenger.Models.UpUsers;
import com.example.upmessenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    TextView textView;
    RecyclerView userRecycler;
    UserAdapter mUserAdapter;
    ArrayList<String> users;

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");

        View view = inflater.inflate(R.layout.fragment_chat,container,false);

        users = new ArrayList<>();

        userRecycler = view.findViewById(R.id.userRecycler);
        userRecycler.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        Drawable mDivider = ContextCompat.getDrawable(getActivity(), R.drawable.divider);

        DividerItemDecoration vItemDecoration = new DividerItemDecoration(userRecycler.getContext(),
                DividerItemDecoration.VERTICAL);
        // Set the drawable on it
        vItemDecoration.setDrawable(mDivider);
        userRecycler.addItemDecoration(vItemDecoration);

        mUserAdapter = new UserAdapter(inflater,inflater.getContext());
        userRecycler.setAdapter(mUserAdapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String text = "";
                    users.clear();
                    for (DataSnapshot child:snapshot.getChildren()){
//                        UpUsers upUsers = child.getValue(UpUsers.class);
////                        Log.d("EMAIL",upUsers.getName());
//                        text = text +"\n  "+ upUsers.getName() +" "+upUsers.getEmail();
//                        textView.setText(text);
                            Log.d("CHILD_KEY",child.getKey());
                            users.add(child.getKey());
                    }
                    mUserAdapter.setUsers(users);

//                    Dialog.dismiss();
                //                UpUsers upuser = snapshot.getValue(UpUsers.class);
//                Log.d("UPUSER",upuser.getEmail());
//                text = text + upuser.getName();
//                textView.setText(text);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}