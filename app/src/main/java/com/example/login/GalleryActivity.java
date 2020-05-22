package com.example.login;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateFormat;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.net.URISyntaxException;
//import java.text.DateFormat;

public class GalleryActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView description;
    private FirebaseListAdapter<ChatMessage> adapter;
    RelativeLayout activity_galery;
    FloatingActionButton fab;
    public TextView messageText, messageUser, messageTime, messageToWhom;
    public String thisprofile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galery);
        Upload note = null;
        if(getIntent().hasExtra("selected_item")) {
            note = getIntent().getParcelableExtra("selected_item");
        }
        activity_galery=findViewById(R.id.activity_galery);
        imageView=findViewById(R.id.image_note);
        description=findViewById(R.id.image_description);
        fab=findViewById(R.id.fab);
        thisprofile=note.getEmail();

        final EditText input=findViewById(R.id.input);
        input.setOnEditorActionListener(editorListener);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail(), thisprofile));
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
        description.setText("Name: " + note.getName() + "\n" + "City: " + note.getCity() + "\n" + "Job: " +note.getJob());

        Snackbar.make(activity_galery, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();

        displayChatMessage(thisprofile);
    }

    private void displayChatMessage(final String whichprofile) {
        ListView listofmessages=findViewById(R.id.list_of_messages);
        adapter=new FirebaseListAdapter<ChatMessage>(GalleryActivity.this, ChatMessage.class, R.layout.list_item, FirebaseDatabase.getInstance().getReference()){
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                messageText = v.findViewById(R.id.message_text);
                messageUser = v.findViewById(R.id.message_user);
                messageTime = v.findViewById(R.id.message_time);
                if(model.getMessageToWhom()!=null && model.getMessageUser()!=null)
                    /*if((model.getMessageUser().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()) && model.getMessageToWhom().equals(whichprofile))
                    || (model.getMessageUser().equals(whichprofile) && model.getMessageToWhom().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())))*/
                    if(model.getMessageToWhom().equals(whichprofile))
                    {
                        messageText.setText(model.getMessageText());
                        messageUser.setText(model.getMessageUser());
                        messageTime.setText(DateFormat.format("yyyy:MM:dd", model.getMessageTime()));
                    }
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
