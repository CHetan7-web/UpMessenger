package com.example.upmessenger.Adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.upmessenger.Models.UpLastMessage;
import com.example.upmessenger.Models.UpUsers;
import com.example.upmessenger.R;
import com.example.upmessenger.UserOnClick;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    FirebaseDatabase database;
    DatabaseReference usersRef, msgRef;
    LayoutInflater mLayoutInflater;
    Context mContext;
    ArrayList<String> users;
    UserOnClick userOnClick;
    String senderId;

    public UserAdapter(LayoutInflater mLayoutInflater, UserOnClick userOnClickInterface, Context mContext) {
        this.mContext = mContext;
        this.mLayoutInflater = mLayoutInflater;
        this.users = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = database.getReference("Users");
        msgRef = database.getReference("Users-Connected").child(senderId);
        userOnClick = userOnClickInterface;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.user_list, parent, false);
        UserHolder userHolder = new UserHolder(view);
        return userHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {

        final String[] lastMessage = new String[1];
        String userId = users.get(position);
//        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        DatabaseReference recieverState = database.getReference("Users-Connected/" + userId + "/" + senderId + "/typing");

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UpUsers upUser = snapshot.getValue(UpUsers.class);
                Log.d("USER_DATA", upUser.toString());

                if (upUser != null) {

                    if (upUser.getName() != null)
                        holder.profileName.setText(upUser.getName());

                    //From Users Connected
//                    if (upUser.getLastMessage() != null)
//                        holder.profileMessage.setText(upUser.getLastMessage());
//                    else
//                        holder.profileMessage.setText("Tap to Message ");
//
//                    if (upUser.getTime() != 0)
//                        holder.lastTime.setText(dateFormat.format(upUser.getTime()));

                    if (upUser.getProfilePic() != null)
                        Glide.with(holder.itemView.getContext()).load(upUser.getProfilePic()).apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_foreground)).into(holder.profileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        msgRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UpLastMessage upLastMessage = snapshot.getValue(UpLastMessage.class);
                Log.d("USER_Message_DATA", upLastMessage.toString());

                if (upLastMessage.getLastMessage() != null) {
                    lastMessage[0] = upLastMessage.getLastMessage();
                    holder.profileMessage.setText(upLastMessage.getLastMessage());
                } else
                    holder.profileMessage.setText("Tap to Message ");

                if (upLastMessage.getLastTime() != 0)
                    holder.lastTime.setText(getDateFormated("SHORT", upLastMessage.getLastTime()));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recieverState.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer isTyping = snapshot.getValue(Integer.class);
                Log.d("IS_NULL",String.valueOf(isTyping)+" "+(isTyping != null));
                if (isTyping != null) {
                    if (isTyping == 1)
                        holder.profileMessage.setText("Typing");
                    else {
                        holder.profileMessage.setText(lastMessage[0]);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private String getDateFormated(String formatDate, Long time) {
        DateFormat dateFormat;
        if (formatDate.equals("SHORT"))
            dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        else
            dateFormat = new SimpleDateFormat(formatDate);

        Date msgDate = new Date(dateFormat.format(time));
        Date todaysDate = new Date(dateFormat.format((new Date()).getTime()));

        if (msgDate.compareTo(todaysDate) == 0)
            return new SimpleDateFormat("hh:mm a").format(time);
        else if ((msgDate.getYear() == todaysDate.getYear()) && (msgDate.getMonth() == todaysDate.getMonth()) && (msgDate.getDate() + 1 == todaysDate.getDate()))
            return "YesterDay";
        else {
            return dateFormat.format(msgDate);
        }
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public class UserHolder extends RecyclerView.ViewHolder {

        TextView profileName, profileMessage, lastTime;
        CircleImageView profileImage;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            lastTime = itemView.findViewById(R.id.lastTime);
            profileMessage = itemView.findViewById(R.id.profileMessage);
            profileName = itemView.findViewById(R.id.profileName);

            profileImage = itemView.findViewById(R.id.profileImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userOnClick.userClick(getAdapterPosition());
                }
            });

        }
    }

}
