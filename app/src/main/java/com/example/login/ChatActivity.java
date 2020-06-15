package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatuser, mChat_name, mChat_job;
    private Toolbar chatbar;

    private DatabaseReference mDatabase;
    private TextView titleview, jobname;
    private CircleImageView profilimage;
    private String profil_imageurl;
    FirebaseAuth mAuth;
    private String mCurrentuser_id;
    private Button addbutton, sendbutton;
    private EditText sendmessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mDatabase= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrentuser_id=mAuth.getCurrentUser().getUid();

        chatbar=findViewById(R.id.chat_bar);
        addbutton=findViewById(R.id.chat_add_button);
        sendbutton=findViewById(R.id.chat_send_button);
        sendmessage=findViewById(R.id.chat_user_message);

        setSupportActionBar(chatbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        mChatuser=getIntent().getStringExtra("user_id");
        mChat_name=getIntent().getStringExtra("user_name");
        mChat_job=getIntent().getStringExtra("user_job");

        getSupportActionBar().setTitle(mChat_name);


        mDatabase.child("uploads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload temp = postSnapshot.getValue(Upload.class);
                    if(temp.getUid() != null && temp.getUid().equals(mChatuser)) {
                        profil_imageurl=temp.getImageUrl().toString();
                        Picasso.get().load(profil_imageurl)
                                .placeholder(R.drawable.gotoprofile).into(profilimage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        LayoutInflater layoutInflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        titleview=findViewById(R.id.chat_display_name);
        jobname=findViewById(R.id.chat_job_name);
        profilimage=findViewById(R.id.chat_toolbar_image);

        titleview.setText(mChat_name);
        jobname.setText(mChat_job);

        mDatabase.child("chat").child(mCurrentuser_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatuser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatMap = new HashMap();
                    chatMap.put("chat/" + mCurrentuser_id + "/" + mChatuser,chatAddMap);
                    chatMap.put("chat/" + mChatuser + "/" + mCurrentuser_id ,chatAddMap);

                    mDatabase.updateChildren(chatMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null){ }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    private void sendMessage() {

        String message=sendmessage.getText().toString();
        message.trim();
        if(!TextUtils.isEmpty(message)){
            String current_user_ref="messages/" + mCurrentuser_id + "/" + mChatuser;
            String chat_user_ref="messages/" + mChatuser + "/" + mCurrentuser_id;
            String key = mDatabase.child("messages").child(mCurrentuser_id).child(mChatuser).push().getKey().toString();


            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);

            Map messageUserMap =  new HashMap();
            messageUserMap.put(current_user_ref + "/" + key,messageMap);
            messageUserMap.put(chat_user_ref + "/" + key ,messageMap);

            mDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError !=  null){
                        Log.d("myTag","Error  in sendig meassages");
                    }
                }
            });

        }
    }

}
