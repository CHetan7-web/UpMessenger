package com.example.upmessenger.Adapters;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upmessenger.Extras.MessageHeaderItemDecoration;
import com.example.upmessenger.Models.UpMesssage;
import com.example.upmessenger.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {

    private final int SENDER_VIEWHOLDER = 1;
    private final int RECIEVER_VIEWHOLDER = 2;
    private final int UNREAD_HEADER = 3;

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
        if (viewType == SENDER_VIEWHOLDER) {
            view = mLayoutInflater.inflate(R.layout.chat_sender, parent, false);
            return new SenderHolder(view);
        }
        if (viewType == RECIEVER_VIEWHOLDER) {
            view = mLayoutInflater.inflate(R.layout.chat_reciever, parent, false);
            return new RecieverHolder(view);
        }
        view = mLayoutInflater.inflate(R.layout.unread_header, parent, false);
        return new UnReadHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UpMesssage messsage = chats.get(position);
        if (holder.getClass() == SenderHolder.class) {
            ((SenderHolder) holder).senderMessage.setText(messsage.getMessage());
            ((SenderHolder) holder).senderTime.setText(dateFormat.format(messsage.getTime()));
            if (messsage.getSeen() == 0)
                ((SenderHolder) holder).seenState.setText("Sending");
            else if (messsage.getSeen() == 3)
                ((SenderHolder) holder).seenState.setText("Send");
            else if (messsage.getSeen() == 2)
                ((SenderHolder) holder).seenState.setText("Delievered");
            else
                ((SenderHolder) holder).seenState.setText("Seen");

        } else if (holder.getClass() == RecieverHolder.class) {
            ((RecieverHolder) holder).recieverMessage.setText(messsage.getMessage());
            ((RecieverHolder) holder).recieverTime.setText(dateFormat.format(messsage.getTime()));
        } else if (holder.getClass() == UnReadHolder.class) {
            Log.d("DAY_HEADER", "UNREAD HOLDER CREATED POSITION " + position);
            ((UnReadHolder) holder).unreadHeader.setText((chats.size() - position - 1) + " Unread Messages ");
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
        else if (uID.equals("UnreadMessage"))
            return UNREAD_HEADER;

        return RECIEVER_VIEWHOLDER;

    }

    public class RecieverHolder extends RecyclerView.ViewHolder {

        TextView recieverMessage, recieverTime;

        public RecieverHolder(@NonNull View itemView) {
            super(itemView);

            recieverMessage = itemView.findViewById(R.id.reciverMessage);
            recieverTime = itemView.findViewById(R.id.reciverTime);
        }
    }

    public class SenderHolder extends RecyclerView.ViewHolder {

        TextView senderMessage, senderTime, seenState;

        public SenderHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.senderMessage);
            senderTime = itemView.findViewById(R.id.senderTime);
            seenState = itemView.findViewById(R.id.seenState);

        }
    }

    private class UnReadHolder extends RecyclerView.ViewHolder {
        TextView unreadHeader;

        public UnReadHolder(@NonNull View itemView) {
            super(itemView);
//            Log.d("DAY_HEADER","UNREAD HOLDER CREATED POSITION "+getAdapterPosition());
            unreadHeader = itemView.findViewById(R.id.unreadHeader);
        }
    }

}
