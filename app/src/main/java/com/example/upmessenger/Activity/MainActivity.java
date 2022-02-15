package com.example.upmessenger.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.upmessenger.Adapters.FragmentAdapter;
import com.example.upmessenger.Models.UpUsers;
import com.example.upmessenger.R;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
import com.example.upmessenger.Service.NotificationService;
import com.example.upmessenger.Service.UpMessageService;
import com.google.android.material.tabs.TabLayout;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    TextView main_text;
    FirebaseUser currentUser ;
    FirebaseDatabase db;
    DatabaseReference dbRef;

    TabLayout tabLayout;
    ViewPager viewPager;

    UpUsers UpUser;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("Messages");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null){
            Intent intent = new Intent(this,SignInActivity.class);
            Toast.makeText(getApplicationContext(),"Please Sign in First !!",Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }

        viewPager = findViewById(R.id.home_page_viewer);
        viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));

        tabLayout = findViewById(R.id.home_tab);
        tabLayout.setupWithViewPager(viewPager);

//        startForegroundService(new Intent(getBaseContext(), NotificationService.class));
        startService(new Intent(getBaseContext(), NotificationService.class));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signout_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.signOut:
                Toast.makeText(this,"Signed out",Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();

                Intent intent = new Intent(this,SignInActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Toast.makeText(this,"Signed out",Toast.LENGTH_SHORT).show();
                startActivity(intent);

                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("DEVICE_TOKEN",getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty"));
        if(currentUser == null){
            Intent intent = new Intent(this,SignInActivity.class);
            Toast.makeText(getApplicationContext(),"Please Sign in First !!",Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }else {
            FirebaseDatabase.getInstance().getReference("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/deviceToken")
                    .setValue(getApplicationContext().getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty"));
        }

    }
}
