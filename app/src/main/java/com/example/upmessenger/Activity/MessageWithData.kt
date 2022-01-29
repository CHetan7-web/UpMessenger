package com.example.upmessenger.Activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.upmessenger.Adapters.MessageWithDataAdapter
import com.example.upmessenger.Models.UpMesssage
import com.example.upmessenger.OnSharedClick
import com.example.upmessenger.databinding.ActivityMessageWithDataBinding
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageWithData : AppCompatActivity(), OnSharedClick {

    lateinit var userIds: List<String>
    lateinit var selectedUsersIds:ArrayList<String>
    lateinit var sendUserAdapter: MessageWithDataAdapter
    private lateinit var userRefFirebase: DatabaseReference
    lateinit var activityMessageWithDataBinding: ActivityMessageWithDataBinding

    private lateinit var usersConnected: DatabaseReference
    private lateinit var messagesRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMessageWithDataBinding = ActivityMessageWithDataBinding.inflate(layoutInflater)
        setContentView(activityMessageWithDataBinding.root)

        supportActionBar?.hide()

        activityMessageWithDataBinding.sendUserRecyclerView.layoutManager =
            LinearLayoutManager(this)

        usersConnected = FirebaseDatabase.getInstance().getReference("Users-Connected")
        messagesRef = FirebaseDatabase.getInstance().getReference("Messages")
        userRefFirebase = usersConnected.child(FirebaseAuth.getInstance().currentUser!!.uid)


        userIds = ArrayList<String>()
        selectedUsersIds = ArrayList<String>()

        sendUserAdapter = MessageWithDataAdapter(userIds, this)

        activityMessageWithDataBinding.sendUserRecyclerView.adapter = sendUserAdapter

        userRefFirebase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (userIds as ArrayList<String>).clear()

                for (child: DataSnapshot? in snapshot.getChildren()) {
                    if (child!!.getKey() != FirebaseAuth.getInstance().currentUser!!.uid) {
                        Log.d("CHILD_KEY", child.getKey().toString())

                        (userIds as ArrayList<String>).add(0, child.getKey().toString())
                    }
                }
                sendUserAdapter.sendUsersList = userIds
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        activityMessageWithDataBinding.sendMessage.setOnClickListener(object :View.OnClickListener {
            override fun onClick(v: View?) {
                sendMessage(selectedUsersIds)
            }
        })

    }

    override fun sendMessage(users: List<String>) {
        val senderID = FirebaseAuth.getInstance().currentUser?.uid
        var idSenderReciever: String
        var idRecieverSender: String

        val msg = intent?.getStringExtra(Intent.EXTRA_TEXT)

        if (intent?.type == "text/plain" && msg?.isNotEmpty() == true) {

            var dateFormat: DateFormat = SimpleDateFormat("hh:mm a")
            var date = Date()

            val updateUser = HashMap<String, Any>()
            updateUser["lastMessage"] = msg
            updateUser["lastTime"] = date.time

            if (users.size == 1) {
                //Open Intent with message in msgbox
                var messageActivityIntent: Intent =
                    Intent(this@MessageWithData, MessagesActivity::class.java)
                messageActivityIntent.putExtra("SHARED_MESSAGE", msg)
                messageActivityIntent.putExtra("userKey",users[0])
                startActivity(messageActivityIntent)
            } else {
                //For multiple users send at that moement only

                for (user in users) {
                    idSenderReciever = senderID + user
                    idRecieverSender = user + senderID

                    var upMessage = UpMesssage(senderID, msg, updateUser["lastTime"] as Long)

                    messagesRef.child(idSenderReciever).push().setValue(upMessage)
                        .addOnSuccessListener {
                            object : OnSuccessListener<Void?> {
                                override fun onSuccess(unused: Void?) {
//                            senderRef.updateChildren(updateUser)
                                    usersConnected.child(senderID!!).child(user)
                                        .updateChildren(updateUser)
                                }

                            }
                        }

                    messagesRef.child(idRecieverSender).push().setValue(upMessage)
                        .addOnSuccessListener {
                            object : OnSuccessListener<Void?> {
                                override fun onSuccess(p0: Void?) {
                                    usersConnected.child(user!!).child(senderID!!)
                                        .updateChildren(updateUser)
                                }

                            }
                        }

                    Log.w("SHARED MESSAGE SENT","Message sent to user "+user)

                }
                Log.w("SHARED MESSAGE SENT","All messages sent")

                var mainActivityIntent: Intent? =
                    Intent(this@MessageWithData, MainActivity::class.java)
                startActivity(mainActivityIntent)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun updateSendButtonUI(selectedUsers: MutableMap<String, String>, hide: Boolean) {

        if (hide) {
            activityMessageWithDataBinding.sendSelectedUsers.visibility = View.GONE
            activityMessageWithDataBinding.usersSelectedNames.text = ""
        } else {
            selectedUsersIds.clear()
            activityMessageWithDataBinding.sendSelectedUsers.visibility = View.VISIBLE
            var userString = ""
            for ((k, v) in selectedUsers) {
                userString = v + "," + userString
                selectedUsersIds.add(k)
            }

            userString = selectedUsers.size.toString() + " Users: " + userString
            activityMessageWithDataBinding.usersSelectedNames.text = userString
        }
    }
}
