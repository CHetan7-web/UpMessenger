package com.example.upmessenger.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.upmessenger.Models.UpUsers;
import com.example.upmessenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.database.DatabaseReference;



public class ChatFragment extends Fragment {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    TextView textView;

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");

        View view = inflater.inflate(R.layout.fragment_chat,container,false);
        textView = (TextView) view.findViewById(R.id.chat_text);
        ProgressDialog Dialog = new ProgressDialog(getContext());
        Dialog.setMessage("Doing something...");
        Dialog.show();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String text = "";
                    for (DataSnapshot child:snapshot.getChildren()){
                        UpUsers upUsers = child.getValue(UpUsers.class);
//                        Log.d("EMAIL",upUsers.getName());
                        text = text +"\n  "+ upUsers.getName() +" "+upUsers.getEmail();
                        textView.setText(text);

                    }
                    Dialog.dismiss();
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