package com.example.databinding_recyclerview

//import com.example.databinding_recyclerview.databinding.ProductRowBinding
import android.content.Intent
import android.view.View
import com.example.upmessenger.Activity.MainActivity
import com.example.upmessenger.Activity.SignInActivity
import com.google.android.gms.tasks.OnSuccessListener
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class UserRecyclerAdapter{//(userList: List<User>) : RecyclerView.Adapter<UserViewHolder?>() {
//    var userList: List<User>
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
//        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.getContext())
//        val productRowBinding: ProductRowBinding =
//            ProductRowBinding.inflate(layoutInflater, parent, false)
//        return UserViewHolder(productRowBinding)
//    }
//
//var intent: Intent? = Intent(this@SignUpActivity, MainActivity::class.java)
//    startActivity(intent)
//
//    senderMsgRef.push().setValue(upMesssage).addOnSuccessListener(
//    object : OnSuccessListener<Void?> {
//        override fun onSuccess(unused: Void?) {
//            senderRef.updateChildren(updateUser)
//            senderUsers.child(reciverId).child("lastTime").setValue(updateUser.get("lastTime"))
//        }
//    })
//    tvSignIn.setOnClickListener(
//    object : View.OnClickListener {
//        override fun onClick(v: View) {
//            val intent = Intent(getApplicationContext(), SignInActivity::class.java)
//            startActivity(intent)
//        }
//    })
//    var dateFormat: DateFormat = SimpleDateFormat("hh:mm a")
//    var date = Date()
//
//    fun setUsers(users: ArrayList<String?>) {
//        users = users
//        notifyDataSetChanged()
//    }
//
//    @BindingAdapter("android:loadProfileImage")
//    public static void loadImage(ImageView imageView,String imageUrl){
//        Glide.with(imageView.getContext())
//            .load(imageUrl)
//            .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_foreground))
//            .into(imageView);
//
//    }
//
//    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
//        val user: User = userList[position]
//        holder.productRowBinding.setUser(user)
//        holder.productRowBinding.executePendingBindings()
//    }
//
//    users.clear()
//    for (  child:DataSnapshot? in snapshot.getChildren())
//    {
//        if (child.getKey() != FirebaseAuth.getInstance().currentUser!!.uid) {
//            Log.d("CHILD_KEY", child.getKey())
//            users.add(0, child.getKey())
//        }
//    }
//
//    usersRef.child(userId).addListenerForSingleValueEvent(
//    object : ValueEventListener {
//        override fun onDataChange(snapshot: DataSnapshot) {
//            val upUser = snapshot.getValue(UpUsers::class.java)
//            Log.d("USER_DATA", upUser.toString())
//            if (upUser != null) {
//                if (upUser.name != null) holder.profileName.setText(upUser.name)
//                if (upUser.lastMessage != null) holder.profileMessage.setText(upUser.lastMessage) else holder.profileMessage.setText(
//                    "Tap to Message "
//                )
//                if (upUser.time != 0L) holder.lastTime.setText(dateFormat.format(upUser.time))
//                if (upUser.profilePic != null) Glide.with(holder.itemView.getContext())
//                    .load(upUser.profilePic)
//                    .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_foreground))
//                    .into(holder.profileImage)
//            }
//        }
//
//        override fun onCancelled(error: DatabaseError) {}
//    })
//
//
//    override fun getItemCount(): Int {
//        return userList.size
//    }
//
//    internal inner class UserViewHolder(productRowBinding: ProductRowBinding) : RecyclerView.ViewHolder(productRowBinding.getRoot()) {
//
//        var productRowBinding: ProductRowBinding
//
//        init {
//            this.productRowBinding = productRowBinding
//            productRowBinding.activeButton.setOnClickListener(View.OnClickListener {
//                Log.d(
//                    TAG,
//                    "onClick: " + userList[getAdapterPosition()]
//                )
//            })
//        }
//    }
//
//    companion object {
//        private const val TAG = "UserRecyclerAdapter"
//    }
//
//    init {
//        this.userList = userList
//    }
}