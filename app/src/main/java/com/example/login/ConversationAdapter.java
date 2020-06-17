package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ItemHolder> {

    Context context;
    List<Messages> usersArrayList;
    private DatabaseReference mDatabase;
    String user_name, user_id, user_job;
    private OnNoteListener mOnNoteListener;
    //private ConversationAdapter.OnNoteListener mOnNoteListener;

    public ConversationAdapter(Context context, List<Messages> usersArrayList, OnNoteListener onNoteListener) {
        this.context = context;
        this.usersArrayList = usersArrayList;
        mOnNoteListener=onNoteListener;
    }

    @NonNull
    @Override
    public ConversationAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_conversation, null);
        ConversationAdapter.ItemHolder itemHolder = new ConversationAdapter.ItemHolder(v, mOnNoteListener);
        return itemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemHolder holder, int position) {
        final Messages friends = usersArrayList.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(friends.getTime());
        SimpleDateFormat curr_time = new SimpleDateFormat("MM.dd. ~ HH:mm");
        String dateString = curr_time.format(calendar.getTime());

        String chat_user_id=friends.getFrom();
        holder.txtStatus.setText(dateString);

        mDatabase= FirebaseDatabase.getInstance().getReference();
        Log.d("chatTag", "onbindview, uploads listener elott");

        mDatabase.child("uploads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload temp = postSnapshot.getValue(Upload.class);
                    if(temp.getUid() != null && temp.getUid().equals(friends.getFrom())) {
                        String profil_imageurl=temp.getImageUrl().toString();
                        Picasso.get().load(profil_imageurl)
                                .placeholder(R.drawable.gotoprofile).into(holder.imageView);
                       holder.txtName.setText(temp.getName());
                       user_id=temp.getUid();
                       user_name=temp.getName();
                       user_job=temp.getJob();

                       if(friends.getMdata().length()>20){
                           String first20= friends.getMdata().substring(0, 20);
                           holder.txtMessage.setText(first20 + "...");
                       }else
                           holder.txtMessage.setText(friends.getMdata());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CircleImageView imageView;
        public TextView txtName, txtStatus, txtMessage;
        OnNoteListener onNoteListener;

        public ItemHolder(View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            imageView = (CircleImageView) itemView.findViewById(R.id.user_single_image);
            txtName = (TextView) itemView.findViewById(R.id.user_single_name);
            txtStatus = (TextView) itemView.findViewById(R.id.user_single_status);
            txtMessage=(TextView)itemView.findViewById(R.id.user_single_message);
            this.onNoteListener=onNoteListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onNoteListener.onNoteClick(getAdapterPosition());
        }
    }

    public interface OnNoteListener{
        void onNoteClick(int position);
    }
}
