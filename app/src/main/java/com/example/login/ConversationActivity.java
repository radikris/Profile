package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConversationActivity extends AppCompatActivity implements ConversationAdapter.OnNoteListener {

    private RecyclerView mConvList;

    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mLastMessage;
    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;
    private String date;

    private String mCurrent_user_id;
    private ConversationAdapter friendsAdapter;
    private final List<Messages> my_messagelist = new ArrayList<Messages>();
    public static ArrayList<String> list_user_friend_key;
    private View mMainView;

    public String user_name, user_id, user_job;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Log.d("chatTag", "chatact oncreate");
        getSupportActionBar().setTitle("My conversations");

        init();

        mMessageDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String user_id = dataSnapshot.getKey().toString();

                mLastMessage.child(user_id).limitToLast(1).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messages.setFrom(user_id);
                        if(messages.getType().equals("text")){
                            date = messages.getMessage();
                        }else {
                            date = "Sent an image";
                        }

                        messages.setMdata(date);
                        //my_messagelist.add(messages);
                        int pos=-1;
                        for(int i=0; i<my_messagelist.size(); i++){
                            if(my_messagelist.get(i).getFrom().equals(messages.getFrom()))
                                pos=i;
                        }
                        if(pos!=-1){
                            my_messagelist.set(pos, messages);
                        }else{
                            my_messagelist.add(messages);
                        }
                        Collections.sort(my_messagelist, new CustomComparator());
                        Log.d("chatTag", "listhez hozzaadjuk");
                        friendsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                /*mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();
                        String userOnline = dataSnapshot.child("online").getValue().toString();

                        if(list_user_friend_key.contains(dataSnapshot.getKey().toString())){
                            int position = list_user_friend_key.indexOf(dataSnapshot.getKey().toString());
                            friendsArrayList.get(position).setOnline_status(userOnline);
                            friendsAdapter.notifyDataSetChanged();
                        }else {
                            list_user_friend_key.add(dataSnapshot.getKey().toString());
                            friendsArrayList.add(new Friends(date, name, image, userOnline));
                            friendsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void init(){
        mConvList = (RecyclerView) findViewById(R.id.convo_list);
        list_user_friend_key = new ArrayList<>();
        friendsAdapter = new ConversationAdapter(ConversationActivity.this,my_messagelist, ConversationActivity.this);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        Log.d("chatTag", "init eleje");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("uploads");
        mUsersDatabase.keepSynced(true);

        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mLastMessage = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ConversationActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);
        mConvList.setAdapter(friendsAdapter);

        Log.d("chatTag", "init vege");

    }

    @Override
    public void onNoteClick(final int position) {
        final Intent intent=new Intent(this, ChatActivity.class);
        mDatabase= FirebaseDatabase.getInstance().getReference();
        Log.d("chatTag", "onbindview, uploads listener elott");

        mDatabase.child("uploads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload temp = postSnapshot.getValue(Upload.class);
                    if(temp.getUid() != null && temp.getUid().equals(my_messagelist.get(position).getFrom())) {
                        String profil_imageurl=temp.getImageUrl().toString();
                        user_id=temp.getUid();
                        user_name=temp.getName();
                        user_job=temp.getJob();

                        intent.putExtra("user_name", user_name);
                        intent.putExtra("user_id", user_id);
                        intent.putExtra("user_job", user_job);
                        startActivity(intent);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class CustomComparator implements Comparator<Messages> {
            @Override
            public int compare(Messages o1, Messages o2) {
                int ret_val=0;
                if(o1.getTime()==o2.getTime())
                    ret_val=0;
                if(o1.getTime()<o2.getTime())
                    ret_val=-1;
                if(o1.getTime()>o2.getTime())
                    ret_val=1;
                return ret_val;
            }
        }

}
