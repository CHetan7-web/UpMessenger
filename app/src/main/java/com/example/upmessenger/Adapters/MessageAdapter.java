package com.example.upmessenger.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upmessenger.Models.UpMesssage;
import com.example.upmessenger.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {

    private final int SENDER_VIEWHOLDER = 1;
    private final int RECIEVER_VIEWHOLDER = 2;

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private ArrayList<UpMesssage> chats;
    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");

    public MessageAdapter(Context mContext, LayoutInflater mLayoutInflater) {
        this.mContext = mContext;
        this.mLayoutInflater = mLayoutInflater;
        this.chats = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType==SENDER_VIEWHOLDER){
            view = mLayoutInflater.inflate(R.layout.chat_sender,parent,false);
            return new SenderHolder(view);
        }
        view = mLayoutInflater.inflate(R.layout.chat_reciever,parent,false);
        return new RecieverHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UpMesssage messsage = chats.get(position);
        if (holder.getClass()==SenderHolder.class){
            ((SenderHolder) holder).senderMessage.setText(messsage.getMessage());
            ((SenderHolder) holder).senderTime.setText(dateFormat.format(messsage.getTime()));
        }else{
            ((RecieverHolder)holder).recieverMessage.setText(messsage.getMessage());
            ((RecieverHolder)holder).recieverTime.setText(dateFormat.format(messsage.getTime()));
        }
    }

    public void setChats(ArrayList<UpMesssage> chats) {
        this.chats = chats;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    @Override
    public int getItemViewType(int position) {

        String uID = chats.get(position).getUserId();

        if (uID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            return SENDER_VIEWHOLDER;
        return RECIEVER_VIEWHOLDER;

    }

    public class RecieverHolder  extends RecyclerView.ViewHolder{

        TextView recieverMessage,recieverTime;

        public RecieverHolder(@NonNull View itemView) {
            super(itemView);

            recieverMessage = itemView.findViewById(R.id.reciverMessage);
            recieverTime = itemView.findViewById(R.id.reciverTime);
        }
    }

    public class SenderHolder extends RecyclerView.ViewHolder{

        TextView senderMessage,senderTime;

        public SenderHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.senderMessage);
            senderTime = itemView.findViewById(R.id.senderTime);

        }
    }

}
