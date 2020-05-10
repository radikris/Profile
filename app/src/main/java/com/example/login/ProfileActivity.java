package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    TextView textView;
    DatabaseReference mDatabaseRef;
    public Upload finalist;
    public EditText meditcity, meditname, meditjob;
    public ImageView imageView;
    public Button updatebuton;
    public String childid;
    private String newcity, newjob, newname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Slidr.attach(this);
        textView=findViewById(R.id.textView);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        meditcity=findViewById(R.id.editcity);
        meditjob=findViewById(R.id.editjob);
        meditname=findViewById(R.id.editname);
        imageView=findViewById(R.id.showprofile);
        updatebuton=findViewById(R.id.updatebutton);

        final FirebaseUser user = mAuth.getCurrentUser();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    User nupload = postSnapshot.getValue(User.class);
                    //upload.setEmail(postSnapshot.getValue(Upload.class).getEmail());
                    if (upload.getEmail().equals(user.getEmail())) {
                        //Toast.makeText(HomeActivity.this, users_email + "\n=\n" + upload.getEmail(), Toast.LENGTH_SHORT).show();
                        upload.setKey(postSnapshot.getKey());
                        upload.setCity(nupload.getCity());
                        upload.setJob(nupload.getJob());
                        upload.setImageUrl(nupload.getImageUrl());
                        childid=postSnapshot.getKey();
                        finalist=upload;
                        Toast.makeText(ProfileActivity.this, finalist.getCity() + " " + finalist.getJob(), Toast.LENGTH_LONG).show();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // yourMethod();
                            }
                        }, 4000);   //5 seconds*/
                        textView.setText(user.getEmail());
                        Picasso.get()
                                .load(finalist.getImageUrl())
                                .fit()
                                .placeholder(R.drawable.loading)
                                .centerCrop()
                                //.resize(500, 400)
                                .into(imageView);
                        meditcity.setHint(finalist.getCity());
                        meditname.setHint(finalist.getName());
                        meditjob.setHint(finalist.getJob());
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        updatebuton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseRef=FirebaseDatabase.getInstance().getReference("uploads").child(childid);
                if(!meditname.getText().toString().equals("")){
                    newname=meditname.getText().toString();
                    finalist.setName(newname);
                }
                if(!meditcity.getText().toString().equals("")){
                    newcity=meditcity.getText().toString();
                    finalist.setCity(newcity);
                }
                if(!meditjob.getText().toString().equals("")){
                    newjob=meditjob.getText().toString();
                    finalist.setJob(newjob);
                }

                mDatabaseRef.setValue(finalist);
                Toast.makeText(ProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
