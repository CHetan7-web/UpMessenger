package com.example.upmessenger.Adapters

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.upmessenger.R
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.upmessenger.OnSharedClick
import com.example.upmessenger.UpUserData
import com.example.upmessenger.databinding.UserSendtoRowBinding
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class MessageWithDataAdapter( sendUsersList: List<String>,onSharedClickListner:OnSharedClick) : RecyclerView.Adapter<MessageWithDataAdapter.MessageWithDataAdapterHolder>(){

    val usersSelected:MutableMap<String,String>
    var hideSendButton:Boolean
    val onSharedClickListner:OnSharedClick
    var sendUsersList:List<String> ? = null
    set(value) {
        field=value
        notifyDataSetChanged()
        Log.d("USERS UPDATED",field?.size.toString())
    }
    private lateinit var userRefFirebase: DatabaseReference

    init {
        this.sendUsersList = sendUsersList
        usersSelected = LinkedHashMap()
        hideSendButton = true
        this.onSharedClickListner = onSharedClickListner
        userRefFirebase = FirebaseDatabase.getInstance().getReference("Users")
    }

    @SuppressLint("ResourceAsColor")
    inner class MessageWithDataAdapterHolder(userSendtoRowBinding: UserSendtoRowBinding) : RecyclerView.ViewHolder(userSendtoRowBinding.root) {

        lateinit var userSendtoRowBinding : UserSendtoRowBinding

        init {
            this.userSendtoRowBinding = userSendtoRowBinding
            //view select
            userSendtoRowBinding.userRowParent.setOnClickListener(View.OnClickListener {
                Log.d("ON CLICK","User Clicked . User Id : "+sendUsersList!![adapterPosition] )
                Log.d("ON CLICK","User Clicked . User Id : "+ userSendtoRowBinding.sendUser?.name +" "+userSendtoRowBinding.sendUser?.userId )

                if (hideSendButton){
                    //Open Message intent with data
                    onSharedClickListner.sendMessage(arrayListOf<String>(sendUsersList!![adapterPosition]))

                }else{
                    if(userSendtoRowBinding.sendUser?.selected==1){
                        userSendtoRowBinding.sendUser?.selected = 0
                        userSendtoRowBinding.isUserSelected.visibility = View.GONE
                        userSendtoRowBinding.userRowContainer.setBackgroundResource(R.color.upMessengerColor)
                        usersSelected.remove(sendUsersList!![adapterPosition])
                        if (usersSelected.size==0)
                            hideSendButton = true
                    }
                    else{
                        userSendtoRowBinding.sendUser?.selected = 1
                        userSendtoRowBinding.isUserSelected.visibility = View.VISIBLE
                        userSendtoRowBinding.userRowContainer.setBackgroundResource(R.color.userSelected)
                        usersSelected[sendUsersList!![adapterPosition]]=if (userSendtoRowBinding.sendUser?.name ==null) "UpUser" else userSendtoRowBinding.sendUser?.name.toString()
                        if (usersSelected.size==1)
                            hideSendButton=false
                    }
                    onSharedClickListner.updateSendButtonUI(usersSelected,hideSendButton)
                }
            })

//            //multiple selection trigerred through longclick
            userSendtoRowBinding.userRowParent.setOnLongClickListener(View.OnLongClickListener {
                Log.d("ON CLICK","User Long Clicked . User Id : "+sendUsersList!![adapterPosition] )

                if (userSendtoRowBinding.sendUser?.selected == 1){
                    userSendtoRowBinding.sendUser?.selected= 0
                    userSendtoRowBinding.isUserSelected.visibility = View.GONE
                    userSendtoRowBinding.userRowContainer.setBackgroundColor(ContextCompat.getColor(userSendtoRowBinding.root.context,R.color.upMessengerColor))
                    usersSelected.remove(sendUsersList!![adapterPosition])
                    if (usersSelected.size == 0)
                    {
                        hideSendButton = true
                        //Hide Send Button through interface
                    }
                    //Update names in Send Button Text
//                        userSendtoRowBinding.sendUser.isUserSelected(false)
                }
                else{
                    userSendtoRowBinding.sendUser?.selected= 1
                    userSendtoRowBinding.isUserSelected.visibility = View.VISIBLE
                    userSendtoRowBinding.userRowContainer.setBackgroundResource(R.color.userSelected)
                    usersSelected[sendUsersList!![adapterPosition]]= if (userSendtoRowBinding.sendUser?.name ==null) "UpUser" else userSendtoRowBinding.sendUser?.name.toString()
                    if (usersSelected.size == 1){
                        hideSendButton = false
                        //Make Send Button Visible
                    }
                    //Update Names in Send Button Text
                }
                Log.d("ON Clic",usersSelected.size.toString()+" "+hideSendButton)
                onSharedClickListner.updateSendButtonUI(usersSelected,hideSendButton)
//                updateSendButton(usersSelected,hideSendButton)

                return@OnLongClickListener true
            })

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageWithDataAdapterHolder {
        val inflator:LayoutInflater = LayoutInflater.from(parent.context)
        val userSendToRowBinding: UserSendtoRowBinding = UserSendtoRowBinding.inflate(inflator,parent,false)
        return MessageWithDataAdapterHolder(userSendToRowBinding)
    }

    override fun onBindViewHolder(holder: MessageWithDataAdapterHolder, position: Int) {

        val userId = sendUsersList!![position]
        Log.d("USER FOUND",userId)
        userRefFirebase?.child(userId)?.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val upUser = snapshot?.getValue(UpUserData::class.java)
//                val upUser = snapshot?.getValue()
                Log.d("USER FOUND",upUser.toString())
                Log.d("USER FOUND","Hello")

//                val upUser:UpUsers = UpUsers()
                holder.userSendtoRowBinding.sendUser=upUser
                holder.userSendtoRowBinding.executePendingBindings()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("User Not Found ",error.toString())
            }
        });

    }

    override fun getItemCount() = sendUsersList!!.size

}

