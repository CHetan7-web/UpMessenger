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
import com.example.upmessenger.Activity.AddUserActivity;
import com.example.upmessenger.Models.UpUsers;
import com.example.upmessenger.R;
import com.example.upmessenger.UserOnClick;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewUserAdapter extends RecyclerView.Adapter<NewUserAdapter.NewUserHolder>{

    FirebaseDatabase database ;
    DatabaseReference usersRef;
    LayoutInflater mLayoutInflater;
    Context mContext;
    ArrayList<String > users;
    UserOnClick userOnClick;

    public NewUserAdapter(LayoutInflater mLayoutInflater, UserOnClick userOnClickInterface, Context mContext) {
        this.mContext = mContext;
        this.mLayoutInflater = mLayoutInflater;
        this.users = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");
        userOnClick = userOnClickInterface;
    }

    @NonNull
    @Override
    public NewUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.user_list,parent,false);
        NewUserHolder userHolder = new NewUserHolder(view);
        return userHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NewUserHolder holder, int position) {

        String userId = users.get(position);
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UpUsers upUser = snapshot.getValue(UpUsers.class);
                Log.d("USER_DATA",upUser.toString());

                if(upUser!=null) {

                    if (upUser.getName() != null)
                        holder.profileName.setText(upUser.getName());

                    if (upUser.getProfilePic() != null)
                        Glide.with(holder.itemView.getContext()).load(upUser.getProfilePic()).apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_foreground)).into(holder.profileImage);

                    if (upUser.getStatus() != null)
                        holder.profileMessage.setText(upUser.getStatus());
                    else
                        holder.profileMessage.setText("Hey! I am using UpMessenger");


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

    public void setUsers(ArrayList<String> users){
        this.users = users;
        notifyDataSetChanged();
    }

    public class NewUserHolder extends RecyclerView.ViewHolder{

        TextView profileName,profileMessage;
        CircleImageView profileImage;

        public NewUserHolder(@NonNull View itemView) {
            super(itemView);

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
