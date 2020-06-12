package com.example.login;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateFormat;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.example.login.ChatMessage;
import com.example.login.Ratingsclass;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
//import java.text.DateFormat;

public class GalleryActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView description;
    private TextView ratingsummary;
    private FirebaseListAdapter<ChatMessage> adapter;
    private FirebaseListAdapter<ChatMessage> tempadapter;
    RelativeLayout activity_galery;
    FloatingActionButton fab;
    public TextView messageText, messageUser, messageTime, messageToWhom;
    public String thisprofile;
    public EditText input;
    public String openlink;
    private int countofmessages;
    private int ratingstars;
    RatingBar ratingbar;
    DatabaseReference mDatabaseRef;
    DatabaseReference mDatabaseRef_ratings;
    FirebaseAuth mAuth;
    public String ratingchildid, ratingman;
    public float sumofstars, numofratings, defaultstars;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galery);
        Upload note = null;
        if (getIntent().hasExtra("selected_item")) {
            note = getIntent().getParcelableExtra("selected_item");
        }

        activity_galery = findViewById(R.id.activity_galery);
        imageView = findViewById(R.id.image_note);
        description = findViewById(R.id.image_description);
        fab = findViewById(R.id.fab);
        ratingsummary=findViewById(R.id.ratingsummary);
        thisprofile = note.getEmail();
        mAuth=FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("comments");
        mDatabaseRef_ratings = FirebaseDatabase.getInstance().getReference("ratings");

        ratingbar=(RatingBar)findViewById(R.id.rb_ratingBar);
        ratingstars=0;
        numofratings=0;
        sumofstars=0;


        mDatabaseRef.child(note.getKey());
        final Upload finalNote = note;
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    // The child doesn't exist
                    mDatabaseRef.push().setValue(finalNote.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //
            }
         });

        mDatabaseRef_ratings.child(note.getKey());
        mDatabaseRef_ratings.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    // The child doesn't exist
                    //mDatabaseRef_ratings.push().setValue(finalNote.getKey());
                    mDatabaseRef_ratings.push().setValue(new Ratingsclass("temp", 5));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //
            }
        });

         mDatabaseRef = FirebaseDatabase.getInstance().getReference("comments").child(note.getKey());
         mDatabaseRef_ratings = FirebaseDatabase.getInstance().getReference("ratings").child(note.getKey());
            //mDatabaseRef = FirebaseDatabase.getInstance().getReference("comments");
        mDatabaseRef_ratings.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    numofratings=0;
                    sumofstars=0;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Log.d("myTag", "ondatachange postsnaphot megvan + " + postSnapshot.getKey());
                        Ratingsclass temp = postSnapshot.getValue(Ratingsclass.class);
                        if(temp.getRatingfromwho().equals(mAuth.getCurrentUser().getEmail())){
                            ratingchildid=postSnapshot.getKey();
                            Log.d("myTag", "Igen, megvan az ertekelo! " + ratingchildid);
                        }
                        numofratings+=1;
                        sumofstars+=temp.getNum_of_stars();
                        Log.d("myTag", numofratings + " es " + sumofstars);
                    }
                    if(numofratings==0)
                        defaultstars=0;
                    else
                        defaultstars=sumofstars/numofratings;
                    String result = String.format("%.2f", defaultstars);
                    ratingsummary.setText("Ratings: (" + (int)numofratings + ") " + result);
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Log.d("myTag", defaultstars + " na?");

        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingstars=(int)rating;
                if(ratingchildid==null)
                    mDatabaseRef_ratings.push().setValue(new Ratingsclass(mAuth.getCurrentUser().getEmail(), ratingstars));
                else{
                    //mDatabaseRef_ratings.child(ratingchildid).removeValue();         ez rontott el dolgokat
                    mDatabaseRef_ratings = FirebaseDatabase.getInstance().getReference("ratings").child(finalNote.getKey()).child(ratingchildid);
                    Ratingsclass temp=new Ratingsclass(mAuth.getCurrentUser().getEmail(), ratingstars);
                    mDatabaseRef_ratings.setValue(temp);
                    mDatabaseRef_ratings = FirebaseDatabase.getInstance().getReference("ratings").child(finalNote.getKey());
                }
            }
        });


        Log.d("myTag", "oncreate pipa");

        input = findViewById(R.id.input);
        input.setHorizontallyScrolling(false);
        input.setLines(7);
        input.setOnEditorActionListener(editorListener);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input.setOnEditorActionListener(editorListener);
                if (input.getText().toString().trim().equals("")) {
                    input.setError("Hmm? It's empty :(");
                } else
                    mDatabaseRef.push().setValue(new ChatMessage(input.getText().toString().trim(), FirebaseAuth.getInstance().getCurrentUser().getEmail(), thisprofile));
                input.setText("");
            }
        });

        Uri myUri = Uri.parse(note.getImageUrl());
        //Toast.makeText(this, note.getImageUrl(), Toast.LENGTH_SHORT).show();
        Picasso.get()
                .load(note.getImageUrl())
                .fit()
                .placeholder(R.drawable.loading)
                .centerCrop()
                //.resize(500, 400)
                .into(imageView);
        description.setText("Name: " + note.getName() + "\n" + "City: " + note.getCity() + "\n" + "Job: " + note.getJob());

        Snackbar.make(activity_galery, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();

        displayChatMessage(note.getKey());
    }

    private void displayChatMessage(final String whichprofile) {
        final ListView listofmessages = findViewById(R.id.list_of_messages);
        Log.d("myTag", "displaychatmessage pipa");

        countofmessages=0;
        final ArrayList<ChatMessage> chatlists = new ArrayList<>();
        mDatabaseRef.addValueEventListener(new ValueEventListener() {       //childevenetlistener?
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("myTag", "addvalueventlistener pipa");
                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    countofmessages+=1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GalleryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        adapter = new FirebaseListAdapter<ChatMessage>(GalleryActivity.this, ChatMessage.class, R.layout.list_item, FirebaseDatabase.getInstance().getReference("comments").child(whichprofile)) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                messageText = v.findViewById(R.id.message_text);
                messageUser = v.findViewById(R.id.message_user);
                messageTime = v.findViewById(R.id.message_time);
                String inside = this.getRef(position).getKey();
                if (model.getMessageText() != null)
                    messageText.setText(model.getMessageText().trim());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("yyyy.MM.dd", model.getMessageTime()));


            }
        };

        listofmessages.setAdapter(adapter);

    }

    private TextView.OnEditorActionListener editorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // do something, e.g. set your TextView here via .setText()
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        }
    };

}
