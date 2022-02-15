package com.example.upmessenger.Application

import android.app.Application
import com.example.upmessenger.UpBroadcastReciever.ConnectivityReceiver
import com.example.upmessenger.UpBroadcastReciever.ConnectivityReceiver.ConnectivityReceiverListener
//import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class UpApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

//        val options = FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).setDatabaseUrl("https://upmessenger-e2ea3-default-rtdb.firebaseio.com/").build()
////        val options = FirebaseOptions.builder()
////            .setCredentials(GoogleCredentials.getApplicationDefault())
////            .setDatabaseUrl("https://upmessenger-e2ea3-default-rtdb.firebaseio.com/")
////            .build()
//
//        FirebaseApp.initializeApp(options)
    }

    fun setConnectivityListener(listener: ConnectivityReceiverListener?) {
        ConnectivityReceiver.connectivityReceiverListener = listener
    }

    companion object {
        @get:Synchronized
        var instance: UpApplication? = null
            private set
    }
}