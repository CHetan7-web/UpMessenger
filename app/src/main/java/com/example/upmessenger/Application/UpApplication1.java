//package com.example.upmessenger.Application;
//
//import android.app.Application;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//
//import java.io.IOException;
//
//public class UpApplication1 extends Application {
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        FirebaseOptions options = null;
//        try {
//            options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.getApplicationDefault())
//                    .setDatabaseUrl("https://<DATABASE_NAME>.firebaseio.com/")
//                    .build();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        FirebaseApp.initializeApp(options);
//    }
//}
