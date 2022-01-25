package com.example.upmessenger.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.upmessenger.Adapters.NewUserAdapter;
import com.example.upmessenger.Adapters.UserAdapter;
import com.example.upmessenger.R;
import com.example.upmessenger.UserOnClick;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddUserActivity extends AppCompatActivity implements UserOnClick {

    RecyclerView addUserRecycler;
    NewUserAdapter mNewUserAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ArrayList<String> users;
    ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        getSupportActionBar().hide();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");

        users = new ArrayList<>();

        backArrow = findViewById(R.id.backArrow);

        addUserRecycler = findViewById(R.id.addUserRecycler);
        addUserRecycler.setLayoutManager(new LinearLayoutManager(this));

        Drawable mDivider = ContextCompat.getDrawable(this, R.drawable.divider);

        DividerItemDecoration vItemDecoration = new DividerItemDecoration(addUserRecycler.getContext(),
                DividerItemDecoration.VERTICAL);
        // Set the drawable on it
        vItemDecoration.setDrawable(mDivider);
        addUserRecycler.addItemDecoration(vItemDecoration);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mNewUserAdapter = new NewUserAdapter(getLayoutInflater(), this,this);
        addUserRecycler.setAdapter(mNewUserAdapter);

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
                mNewUserAdapter.setUsers(users);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void userClick(int postition) {

        Toast.makeText(this,"User Clicked",Toast.LENGTH_SHORT).show();

        String key = users.get(postition);

        Intent chatIntent = new Intent(this, MessagesActivity.class);
//        chatIntent.addFlags(Intent.F);
        chatIntent.putExtra("userKey",key);
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(chatIntent);

    }
}