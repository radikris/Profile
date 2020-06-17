package com.example.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.example.login.MessageAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements MessageAdapter.OnNoteListener {

    private String mChatuser, mChat_name, mChat_job;
    private Toolbar chatbar;

    private DatabaseReference mDatabase;
    private TextView titleview, jobname;
    private CircleImageView profilimage;
    private String profil_imageurl, my_imageurl;
    FirebaseAuth mAuth;
    private String mCurrentuser_id;
    private Button addbutton, sendbutton;
    private EditText sendmessage;

    private RecyclerView mchatlist;

    private final List<Messages> my_messagelist = new ArrayList<Messages>();
    private LinearLayoutManager mlinearlayout;
    private MessageAdapter messageAdapter;

    private SwipeRefreshLayout refreshLayout;
    private static final int GALLERY_PICK=1;
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mDatabase= FirebaseDatabase.getInstance().getReference();
        mImageStorage = FirebaseStorage.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrentuser_id=mAuth.getCurrentUser().getUid();

        chatbar=findViewById(R.id.chat_bar);
        addbutton=findViewById(R.id.chat_add_button);
        sendbutton=findViewById(R.id.chat_send_button);
        sendmessage=findViewById(R.id.chat_user_message);

        mChatuser=getIntent().getStringExtra("user_id");
        mChat_name=getIntent().getStringExtra("user_name");
        mChat_job=getIntent().getStringExtra("user_job");

        messageAdapter=new MessageAdapter(this, my_messagelist, ChatActivity.this);

        mchatlist=(RecyclerView)findViewById(R.id.chat_lists);

        mlinearlayout=new LinearLayoutManager(this);
        refreshLayout=findViewById(R.id.refresh_layout);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
            }
        });

        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryintent=new Intent();
                galleryintent.setType("image/*");
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryintent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });



        mchatlist.setHasFixedSize(true);
        mchatlist.setLayoutManager(mlinearlayout);

        mchatlist.setAdapter(messageAdapter);

        loadMessages();

        setSupportActionBar(chatbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


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
                    if(temp.getUid()!=null && temp.getUid().equals(mCurrentuser_id)){
                        my_imageurl=temp.getImageUrl().toString();
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
        refreshLayout.setRefreshing(false);

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
                sendmessage.setText("");
            }
        });

    }

    private void loadMessages() {
        mDatabase.child("messages").child(mCurrentuser_id).child(mChatuser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                my_messagelist.add(message);

                mchatlist.scrollToPosition(my_messagelist.size()-1);
                refreshLayout.setRefreshing(false);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
            messageMap.put("from", mCurrentuser_id);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageuri=data.getData();

            final String current_user_ref="messages/" + mCurrentuser_id + "/" + mChatuser;
            final String chat_user_ref="messages/" + mChatuser + "/" + mCurrentuser_id;
            final String key = mDatabase.child("messages").child(mCurrentuser_id).child(mChatuser).push().getKey().toString();

            StorageReference filepath=mImageStorage.child("message_images").child(key + ".jpg");
            filepath.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    Uri url = urlTask.getResult();
                    String download_url=url.toString();

                    Map messageMap = new HashMap();
                    messageMap.put("message", download_url);
                    messageMap.put("seen", false);
                    messageMap.put("type", "image");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from", mCurrentuser_id);

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
            });

        }
    }

    @Override
    public void onNoteClick(int adapterPosition) {
        String type = my_messagelist.get(adapterPosition).getType();
        if(type.equals("image")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_img_preview, null);
            builder.setView(view);
            ImageView imgView = (ImageView)view.findViewById(R.id.preview_imgview);
            imgView.setImageBitmap(BitmapFactory.decodeFile(my_messagelist.get(adapterPosition).getMessage()));
            Picasso.get().load(my_messagelist.get(adapterPosition).getMessage())
                    .placeholder(R.drawable.loading).into(imgView);

            builder.setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }
}
