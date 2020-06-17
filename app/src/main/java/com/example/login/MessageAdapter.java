package com.example.login;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<Messages> mlistMessages;
    Context mContext;
    private FirebaseAuth mAuth;
    RelativeLayout message_layout;
    private OnNoteListener mOnNoteListener;

    public MessageAdapter(Context context, List<Messages> mlistMessages, OnNoteListener onNoteListener) {
        this.mlistMessages = mlistMessages;
        mContext=context;
        this.mOnNoteListener=onNoteListener;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        MessageViewHolder holder=  new MessageViewHolder(view, mOnNoteListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        Messages c=mlistMessages.get(position);
        String current_user_id=mAuth.getCurrentUser().getUid();
        int previousposition=position-1;
        String from_user = "";
        String message_type="";
        if(c.getFrom()!=null)
            from_user=c.getFrom();
        if(c.getType()!=null)
            message_type=c.getType();

        if(message_type.equals("text")) {

            if (from_user.equals(current_user_id)) {
                holder.messageText.setBackgroundResource(R.drawable.message_text_otherback);
                holder.messageText.setTextColor(Color.BLACK);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.messageText.getLayoutParams();
                params.setMargins(380, 5, 22, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                holder.messageText.setLayoutParams(params);
                holder.messageText.setGravity(Gravity.RIGHT);
            } else {
                holder.messageText.setBackgroundResource(R.drawable.message_text_background);
                holder.messageText.setTextColor(Color.WHITE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.messageText.getLayoutParams();
                params.setMargins(22, 5, 0, 0);
                //params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                holder.messageText.setLayoutParams(params);
                holder.messageText.setGravity(Gravity.LEFT);
            }
            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.GONE);
            holder.messageText.setVisibility(View.VISIBLE);

        }else{

            if (from_user.equals(current_user_id)) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.messageImage.getLayoutParams();
                params.setMargins(380, 5, 22, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                holder.messageImage.setLayoutParams(params);
            } else {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.messageImage.getLayoutParams();
                params.setMargins(22, 5, 0, 0);
                //params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                holder.messageImage.setLayoutParams(params);
            }

            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(c.getMessage())
                    .placeholder(R.drawable.loading)
                    .fit()
                    .centerCrop()
                    .into(holder.messageImage);
        }

        Calendar calendar = Calendar.getInstance();
        /*calendar.setTimeInMillis(c.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");
        String dateString = sdf.format(calendar.getTime());*/

        if(previousposition>=0){
            Messages curr=mlistMessages.get(position);
            Messages prev=mlistMessages.get(previousposition);
            calendar.setTimeInMillis(curr.getTime());
            SimpleDateFormat curr_time = new SimpleDateFormat("MM.dd. ~ HH:mm");
            String dateString = curr_time.format(calendar.getTime());
            if((curr.getTime()-prev.getTime())>240000){
                holder.messageTime.setText(dateString);
                holder.messageTime.setVisibility(View.VISIBLE);
                Log.d("myTag", String.valueOf(prev.getTime()) + prev.getMessage() + " es " + String.valueOf(curr.getTime()) + curr.getMessage());
            }else{
                Log.d("myTag", "ez kisebb mint 5 perc volt");
                holder.messageTime.setVisibility(View.GONE);
            }
        }else{
            Messages curr=mlistMessages.get(position);
            calendar.setTimeInMillis(curr.getTime());
            SimpleDateFormat curr_time = new SimpleDateFormat("MM.dd. ~ HH:mm");
            String dateString = curr_time.format(calendar.getTime());
            holder.messageTime.setText(dateString);
            holder.messageTime.setVisibility(View.VISIBLE);
        }

        //holder.messageText.setText(c.getMessage());
        //holder.messageTime.setText(dateString);
    }

    @Override
    public int getItemCount() {
        return mlistMessages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView messageText;
        public TextView messageTime;
        public ImageView messageImage;
        public CircleImageView profilimage;
        OnNoteListener onNoteListener;

        public MessageViewHolder(View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.chat_single_message);
            messageTime = (TextView) itemView.findViewById(R.id.chat_single_time);
            messageImage=(ImageView) itemView.findViewById(R.id.chat_single_image);
            //messageTime = (TextView) itemView.findViewById(R.id.message_single_time);
            //profilimage=itemView.findViewById(R.id.chat_single_image);
            this.onNoteListener=onNoteListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onNoteListener.onNoteClick(getAdapterPosition());
        }
    }

    public interface OnNoteListener {
        void onNoteClick(int adapterPosition);
    }
}
