package com.example.upmessenger.Adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.upmessenger.Activity.MessagesActivity;
import com.example.upmessenger.Models.UpMesssage;
import com.example.upmessenger.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {

    private final int SENDER_VIEWHOLDER = 1;
    private final int RECIEVER_VIEWHOLDER = 2;
    private final int UNREAD_HEADER = 3;
    private final Uri collection;
    private final String[] PROJECTION;
    private final String QUERY;
    private final ContentResolver mContentResolver;

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private ArrayList<UpMesssage> chats;
    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");

    public MessageAdapter(Context mContext, LayoutInflater mLayoutInflater) {
        this.mContext = mContext;
        this.mLayoutInflater = mLayoutInflater;
        this.chats = new ArrayList<>();

        collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        PROJECTION = new String[]{MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.MediaColumns.RELATIVE_PATH, MediaStore.MediaColumns._ID};
        QUERY = MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? and " +
                MediaStore.Files.FileColumns.DISPLAY_NAME + " like ?";

        mContentResolver = mContext.getContentResolver();
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

        Log.d("EXTERNAL_STORAGE", " IMAGE_TYPE " + messsage.toString());

        if (holder.getClass() == SenderHolder.class) {
            if (messsage.getMsgType() == UpMesssage.MESSAGE_WITH_TEXT)
                ((SenderHolder) holder).senderMessage.setText(messsage.getMessage());

            ((SenderHolder) holder).backgroundImage.setImageDrawable(null);
            ((SenderHolder) holder).bgImageDesc.setText(null);


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
//            Log.d("DAY_HEADER", "UNREAD HOLDER CREATED POSITION " + position);
            ((UnReadHolder) holder).unreadHeader.setText((chats.size() - position - 1) + " Unread Messages ");
        }

        if (messsage.getMsgType() == UpMesssage.MESSAGE_WITH_IMAGE && (holder.getClass() == SenderHolder.class || holder.getClass() == RecieverHolder.class)) {

            Log.d("EXTERNAL_STORAGE_TYPE", " IMAGE_TYPE " + messsage.toString());

            if (MessagesActivity.mediaExists(MessagesActivity.THUMB_PATH, messsage.getThumbPath())
                    && MessagesActivity.mediaExists(MessagesActivity.IMG_PATH, messsage.getMediaPath())) {

                Uri thumbURI = getMediaUri(MessagesActivity.THUMB_PATH, messsage.getThumbPath());

                Uri imgUri = getMediaUri(MessagesActivity.IMG_PATH, messsage.getMediaPath());

//                Log.d("EXTERNAL_STORAGE", "img_uri " + imgUri.toString() + " thumb_uri " + thumbURI.toString());

                int width = dpToPixel(240f);
                int height = dpToPixel(290f);
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

                Glide.with(mContext)
                        .load(imgUri)
                        .thumbnail(Glide.with(mContext).load(thumbURI))
                        .apply(new RequestOptions().override(width, height))
                        .apply(requestOptions)
                        .into(((SenderHolder) holder).backgroundImage);


            }
        } else if (messsage.getMsgType() == UpMesssage.MESSAGE_WITH_GIF && (holder.getClass() == SenderHolder.class || holder.getClass() == RecieverHolder.class)) {

            Log.d("EXTERNAL_STORAGE", " GIF TYPE : " + messsage.toString());

            if (MessagesActivity.mediaExists(MessagesActivity.THUMB_PATH, messsage.getThumbPath())
                    && MessagesActivity.mediaExists(MessagesActivity.GIF_PATH, messsage.getMediaPath())) {

                Uri thumbURI = getMediaUri(MessagesActivity.THUMB_PATH, messsage.getThumbPath());

                Uri gifUri = getMediaUri(MessagesActivity.GIF_PATH, messsage.getMediaPath());

                ((SenderHolder) holder).bgImageDesc.setText("GIF");

                Log.d("EXTERNAL_STORAGE", "img_uti " + gifUri.toString() + " thumb_uri " + thumbURI.toString());

                int width = dpToPixel(240f);
                int height = dpToPixel(290f);
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

                Glide.with(mContext)
                        .load(thumbURI)
                        .apply(new RequestOptions().override(width, height))
                        .apply(requestOptions)
                        .into(((SenderHolder) holder).backgroundImage);

                ((SenderHolder) holder).backgroundImage.setClickable(true);
                ((SenderHolder) holder).bgImageDesc.setVisibility(View.VISIBLE);
                ((SenderHolder) holder).bgImageDesc.setText("GIF");

                ((SenderHolder) holder).backgroundImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ((SenderHolder) holder).backgroundImage.setClickable(false);


                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

                        Glide.with(mContext)
                                .asGif()
                                .load(gifUri)
                                .apply(new RequestOptions().override(width, height))
                                .apply(requestOptions)
                                .listener(new RequestListener<GifDrawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                        resource.setLoopCount(3);
                                        resource.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                                            @Override
                                            public void onAnimationEnd(Drawable drawable) {

                                                RequestOptions requestOptions = new RequestOptions();
                                                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

                                                //do whatever after specified number of loops complete
                                                ((SenderHolder) holder).bgImageDesc.setVisibility(View.VISIBLE);
                                                ((SenderHolder) holder).backgroundImage.setClickable(true);


                                                Glide.with(mContext)
                                                        .load(thumbURI)
                                                        .apply(new RequestOptions().override(width, height))
                                                        .apply(requestOptions)
                                                        .into(((SenderHolder) holder).backgroundImage);

                                            }
                                        });
                                        return false;
                                    }
                                })
                                .into(((SenderHolder) holder).backgroundImage);

                        ((SenderHolder) holder).bgImageDesc.setVisibility(View.GONE);

                    }
                });

            }

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
        ImageView backgroundImage;

        public RecieverHolder(@NonNull View itemView) {
            super(itemView);

            recieverMessage = itemView.findViewById(R.id.reciverMessage);
            recieverTime = itemView.findViewById(R.id.reciverTime);
            backgroundImage = itemView.findViewById(R.id.backgroundImage);


        }
    }

    public class SenderHolder extends RecyclerView.ViewHolder {

        TextView senderMessage, senderTime, seenState, bgImageDesc;
        ImageView backgroundImage;

        public SenderHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.senderMessage);
            senderTime = itemView.findViewById(R.id.senderTime);
            seenState = itemView.findViewById(R.id.seenState);

            backgroundImage = itemView.findViewById(R.id.backgroundImage);

            bgImageDesc = itemView.findViewById(R.id.bgImageDiscription);

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

    int dpToPixel(float dp) {
        float dip = dp;
        Resources r = mContext.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );

        return (int) px;
    }

    private Uri getMediaUri(String file_path, String file_name) {

        Uri uri = null;

        Cursor cursor = mContentResolver.query(collection, PROJECTION, QUERY, new String[]{"%" + file_path + "%", "%" + file_name + "%"}, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d("EXTERNAL_STORAGE", "File exists");
                cursor.moveToFirst();
                do {
                    String _ID = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String filename = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));

                    Log.d("EXTERNAL_STORAGE_FILE", filename + " : " + _ID);

                    uri = Uri.parse(MediaStore.Images.Media.getContentUri("external").toString() + "/" + _ID);
                    Log.d("EXTERNAL_STORAGE_URI", Uri.parse(MediaStore.Images.Media.getContentUri("external").toString() + "/" + _ID).toString());

                } while (cursor.moveToNext());
            }
        }

        return uri;

    }

}
