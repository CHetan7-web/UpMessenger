package com.example.upmessenger.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.upmessenger.Models.UpLastMessage
import com.example.upmessenger.Models.UpMesssage
import com.example.upmessenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class UpMessageService : FirebaseMessagingService() {

    val users = HashMap<String?, Long?>()
    private val CHANNEL_NAME = "NewMesssages"
    private val GROUP_NAME = "UpMessenger"

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {

        //Create Notification Channel
        val name: String = "UpNotification"
        val channelDescription = "Channel for New Messages"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_NAME, name, importance).apply {
            description = channelDescription
        }

        //Register channel with notification manager
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("newToken", p0);

        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", p0).apply()

    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0!!)

        if (p0.data.size > 0) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            Log.d("UpMessageService", p0.data.toString() + "  " + userId);
            FirebaseDatabase.getInstance().getReference("Users-Connected").child(userId!!)
                .orderByChild("unReadCount")
                .startAfter(0.0).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        users.clear()
                        var totalMesssages = 0
                        //Store list of users with unread msgs
                        for (snp in snapshot.children) {
                            var upLastMessage = snp.getValue(UpLastMessage::class.java)
                            users.put(upLastMessage?.msgSenderId, upLastMessage?.lastMessageSeen)
                            Log.d("UpMessageService",upLastMessage?.msgSenderId + " " + upLastMessage?.unReadCount)
                            totalMesssages += upLastMessage?.unReadCount!!
                        }

                        var summaryNotification = NotificationCompat.Builder(applicationContext,CHANNEL_NAME)
                            .setGroup(GROUP_NAME)
                            .setSmallIcon(R.drawable.facebook_icon)
                            .setContentTitle("${totalMesssages} New Messages")
                            .setGroupSummary(true)
                            .setStyle(NotificationCompat.InboxStyle().setSummaryText("${totalMesssages} Messages from ${users.size} Users"))
                            .build()

                        for (user in users.keys) {
                            Log.d("UpMessageService", user!!)
                            users[user]?.toDouble()?.let {
                                FirebaseDatabase.getInstance().getReference("Messages").child(user + userId)
                                    .orderByChild("time").startAfter(it)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.childrenCount > 0) {
                                                var upMessage: UpMesssage? = null

                                                var notiBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_NAME)
                                                    .setContentTitle("User Name")
                                                    .setAutoCancel(true)
                                                    .setColor(Color.GREEN)
                                                    .setSmallIcon(R.drawable.googleg_standard_color_18)
                                                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                                                    .setGroup(GROUP_NAME)
                                                //Notification Style
                                                val inboxStyle = NotificationCompat.InboxStyle()
                                                inboxStyle.setBigContentTitle("User Name")

                                                for (snps in snapshot.children) {
                                                    upMessage = snps.getValue(UpMesssage::class.java)!!

                                                    Log.d("UpMessageService", upMessage!!.message)

                                                    FirebaseDatabase.getInstance().getReference("Messages/"+upMessage.userId+userId)
                                                        .orderByChild("time").equalTo(upMessage!!.getTime().toDouble())
                                                        .addValueEventListener(object :ValueEventListener {
                                                            @Override
                                                            override fun onDataChange( snapshot:DataSnapshot) {
                                                                for (snp1:DataSnapshot  in snapshot.children) {
                                                                    Log.d("SEEN_MESSAGES", "Changing to Delievering  State");
                                                                    snp1.getRef().child("seen").setValue(2);
                                                                }
                                                                snapshot.getRef().removeEventListener(this);
                                                            }

                                                            @Override
                                                            override fun onCancelled( error:DatabaseError) {

                                                            }
                                                        });

                                                    inboxStyle.addLine(upMessage!!.message)
                                                }

                                                notiBuilder.setContentText(upMessage!!.message)
                                                notiBuilder.setStyle(inboxStyle)

                                                with(NotificationManagerCompat.from(applicationContext))
                                                {
                                                    // notificationId is a unique int for each notification that you must define
                                                    notify(upMessage?.userId.hashCode(), notiBuilder.build() )

                                                }
                                            }
                                            snapshot.ref.removeEventListener(this)
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }
                                    })
                            }
                        }

                        with(NotificationManagerCompat.from(applicationContext)){
                            // notificationId is a unique int for each notification that you must define
                            notify(107,summaryNotification)
                        }
                        snapshot.ref.removeEventListener(this)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }
                })


        }

    }

    fun getToken(context: Context): String? {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty")
    }

}