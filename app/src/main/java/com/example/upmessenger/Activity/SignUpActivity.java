package com.example.upmessenger.Activity;

//import android.support.v7.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.upmessenger.Models.UpUsers;
import com.example.upmessenger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    FirebaseDatabase database ;
    DatabaseReference myRef ;

    private FirebaseAuth mAuth;
// ...

    Button btnSignUp;
    EditText etName,etEmail ,etPassword;
    TextView tvSignIn;

    ProgressDialog proDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        tvSignIn = findViewById(R.id.tvSignIn);

        btnSignUp = findViewById(R.id.btnSignUp);

        proDialog = new ProgressDialog(SignUpActivity.this);
        proDialog.setTitle("Signing Up ..");
        proDialog.setMessage("You are Signing up in some moments .. . ");

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                startActivity(intent);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proDialog.show();
                mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(),etPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                        proDialog.dismiss();
                        if(task.isSuccessful()){
                            UpUsers myuser = new UpUsers(etEmail.getText().toString() ,etPassword.getText().toString(),etName.getText().toString());
//                            String id = mAuth.getCurrentUser().getUid();
                            String id = task.getResult().getUser().getUid();
                            Toast.makeText(getApplicationContext(),"Current id :"+id,Toast.LENGTH_LONG).show();
                            myRef.child(id).setValue(myuser);
                            Log.d("UserAdded","User added successfully");
                            Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                            startActivity(intent);

                        }else{
                            Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }
    }
}