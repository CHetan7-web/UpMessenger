package com.example.upmessenger.Service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.NotificationManagerCompat
import com.example.upmessenger.Activity.MainActivity
import com.example.upmessenger.Activity.MessagesActivity
import com.example.upmessenger.Application.UpApplication
import com.example.upmessenger.R
import com.example.upmessenger.UpBroadcastReciever.ConnectivityReceiver

class NotificationService : Service() ,ConnectivityReceiver.ConnectivityReceiverListener {

//    var myIntent :Intent? = null
//    var myFlags:Int? = null
//    var myStartId :Int ? = null

    var notiID:Int = 0
    val grpKey = "UPGroup"

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(){

        //Create Notification Channel
        val name:String = "UpNotification"
        val channelDescription = "Unread Messages"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("Demo",name,importance).apply { description = channelDescription }

        //Register channel with notification manager
        val notificationManager:NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NOTIFICATION_STATUS","NOTIFICATION SERVICE STARTED")

//        myIntent = intent
//        myFlags = flags
//        myStartId = startId

        UpApplication.instance?.setConnectivityListener(this)

        val intentFilter:IntentFilter = IntentFilter()
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")

        val receiver:ConnectivityReceiver = ConnectivityReceiver()

        registerReceiver(receiver,intentFilter)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("NOTIFICATION_STATUS","NOTIFICATION SERVICE DESTROYED")

//        startForegroundService(new Intent(getBaseContext(), NotificationService.class));
//        startService(Intent(baseContext, NotificationService::class.java))

    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Log.d("NOTIFICATION_STATUS","Internet Status Changed . Current Status is "+ ConnectivityReceiver.isConnected)

        createNotification()

    }

    private fun createNotification(){

        val intent = Intent(this,MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK }
        var pendingIntent = PendingIntent.getActivity(this,0,intent,0)

        var notiBuilder = NotificationCompat.Builder(this,"Demo")
            .setContentTitle("Reciever")
            .setContentText("My message is sending")
            .setSmallIcon(R.drawable.facebook_icon)
//            .setLargeIcon(Bitmap.createBitmap(R.drawable.google))
            .setStyle(NotificationCompat.BigTextStyle().bigText("More Message is Coming More Message is Coming More Message is Coming More Message is Coming More Message is Coming More Message is Coming More Message is Coming More Message is Coming"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(VISIBILITY_PRIVATE)
            .setGroup(grpKey)

        val summaryNoti = NotificationCompat.Builder(this, "Demo")
            //set content text to support devices running API level < 24
            .setContentText("Two new messages")
            .setSmallIcon(R.drawable.facebook_icon)
            //build summary info into InboxStyle template
            .setStyle(NotificationCompat.InboxStyle()
                .addLine("Alex Faarborg Check this out")
                .addLine("Jeff Chang Launch Party")
                .setBigContentTitle("2 new messages")
                .setSummaryText("janedoe@example.com"))
            //specify which group this notification belongs to
            .setGroup(grpKey)
            //set this notification as the summary for the group
            .setGroupSummary(true)
            .build()

        with(NotificationManagerCompat.from(this)){
            // notificationId is a unique int for each notification that you must define
            notify(notiID++,notiBuilder.build())
            notify(5,summaryNoti)
        }

    }
}