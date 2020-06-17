package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.r0adkll.slidr.Slidr;
import com.squareup.picasso.Picasso;

import com.example.login.ShowActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.example.login.ShowActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Button logout;
    FirebaseAuth mFirebaseAuth;

    private FirebaseStorage mStorage;

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private TextView mTextViewShowUploads;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Uri mImageUri;
    public Upload selectedItem;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;
    private ValueEventListener mDBListener;
    static public boolean already;
    public String users_email;

    private List<Upload> mUploads;
    int itsokey=0;
    boolean continue_possible;

    public DrawerLayout dlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        dlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, dlayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        dlayout.addDrawerListener(toggle);
        toggle.syncState();



        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mButtonUpload = findViewById(R.id.button_upload);
        mTextViewShowUploads = findViewById(R.id.text_view_show_uploads);
        mEditTextFileName = findViewById(R.id.edit_text_file_name);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);

        mUploads = new ArrayList<>();

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mStorage = FirebaseStorage.getInstance();

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        itsokey=0;

        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAuth = FirebaseAuth.getInstance();
                final FirebaseUser user = mFirebaseAuth.getCurrentUser();
                users_email = user.getEmail();
                /*mDBListener=mDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        already = false;
                        Toast.makeText(HomeActivity.this, "MOST LETT MEGHIVVA A FIREBASECHANGE", Toast.LENGTH_LONG).show();

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Upload upload = postSnapshot.getValue(Upload.class);
                            //upload.setEmail(postSnapshot.getValue(Upload.class).getEmail());
                            if (upload.getEmail().equals(users_email)) {
                                //Toast.makeText(HomeActivity.this, users_email + "\n=\n" + upload.getEmail(), Toast.LENGTH_SHORT).show();
                                already=true;
                                SharedPreferences.Editor editor = getSharedPreferences("uploads", MODE_PRIVATE).edit();
                                editor.putBoolean(user.getEmail(), true);
                                editor.apply();
                                /*Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // yourMethod();
                                    }
                                }, 1000);   //5 seconds
                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(HomeActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });*/
                //if (!already) {
                    //Toast.makeText(HomeActivity.this, "You already have one profil, delete it first!", Toast.LENGTH_LONG).show();
                //} else {*/
                        if (mUploadTask != null && mUploadTask.isInProgress()) {
                        Toast.makeText(HomeActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                        }else {
                            already=true;
                            uploadFile();
                        }
            //}
            }
        });

        mTextViewShowUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagesActivity();
            }
        });
    }

    private void openFileChooser() {
        CropImage.activity().start(HomeActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result= CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                mImageUri = result.getUri();
                Picasso.get()
                        .load(mImageUri)
                        .fit()
                        .centerCrop()
                        .into(mImageView);
            }else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception e=result.getError();
                Toast.makeText(HomeActivity.this, "Something went wrong, maybe " + e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        /*SharedPreferences.Editor editor = getSharedPreferences("uploads", MODE_PRIVATE).edit();
        editor.putBoolean(user.getEmail(), true);
        editor.apply();*/
        ///INNENTOL MEGY A PROBALGATAS*************************************************************************************

        //final int finalPosition = position;
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String users_email=user.getEmail();
                Toast.makeText(HomeActivity.this, "NA EZ A MSODIK ONCHANGE", Toast.LENGTH_SHORT).show();
                mUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    //upload.setEmail(postSnapshot.getValue(Upload.class).getEmail());
                    upload.setKey(postSnapshot.getKey());
                    User nupload = postSnapshot.getValue(User.class);
                    upload.setCity(nupload.getCity());
                    upload.setJob(nupload.getJob());
                    upload.setImageUrl(nupload.getImageUrl());
                    if(nupload.getUid()!=null){
                        upload.setUid(nupload.getUid());
                    }
                    mUploads.add(upload);
                }

                //itsokey=1;
                if(itsokey==0){
                    int position=-1;
                    for(int i=0; i<mUploads.size(); i++){
                        if((mUploads.get(i).getEmail()).equals(users_email)) {
                            position = i;
                        }
                    }
                    if(position!=-1){
                        //continue_possible=openDialog(position, users_email, "If you pick yes, your previous profile will be deleted!");
                        deleteprofile(position, users_email);
                        selectedItem = mUploads.get(position);
                        InformDialog("Your previous profile has been deleted! Enjoy your new one!");
                    }
                    itsokey+=1;
                        //openDialog(position, users_email, "If you pick yes, your previous profile will be deleted!");
                }

                /*mAdapter = new ProfileAdapter(HomeActivity.this, mUploads);
                mRecyclerView.setAdapter(mAdapter);*/
                //mProgressCircle.setVisibility(View.INVISIBLE);
                //users_email=user.getEmail();
                /*int position=-1;
                for(int i=0; i<mUploads.size(); i++){
                    if((mUploads.get(i).getEmail()).equals(users_email)) {
                        position = i;
                    }
                    //Toast.makeText(ShowActivity.this, mUploads.get(i).getEmail().toString(), Toast.LENGTH_SHORT).show();
                }
                if(position!=-1) {

                    Upload selectedItem = mUploads.get(position);
                    StorageReference imageref = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
                    final String selectedKey = selectedItem.getKey();
                    imageref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mDatabaseRef.child(selectedKey).removeValue();
                            Toast.makeText(HomeActivity.this, "Your profile has been deleted!", Toast.LENGTH_SHORT).show();
                            HomeActivity.already = false;
                            SharedPreferences.Editor editor = getSharedPreferences("uploads", MODE_PRIVATE).edit();
                            editor.putBoolean(users_email, false);
                            editor.apply();
                        }
                    });
                }*/

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                //mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });

        int position=-1;
        for(int i=0; i<mUploads.size(); i++){
            if((mUploads.get(i).getEmail()).equals(users_email)) {
                position = i;
            }
        }
        /*if(position!=-1) {
            openDialog(position, users_email, "If you pick yes, your previous profile will be deleted!");
        }else{
            Toast.makeText(HomeActivity.this, "alma" + mUploads.size(), Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // yourMethod();
                }
            }, 1000);
        }*/
        ///IDEAIG EZ PROBALGATAS****************************************************************************************

        SharedPreferences prefs = getSharedPreferences("uploads", MODE_PRIVATE);
        boolean alreadyupload = prefs.getBoolean(user.getEmail(), false); //0 is the default value.
        //Toast.makeText(HomeActivity.this, "ez: " + alreadyupload, Toast.LENGTH_SHORT).show();

        if(true) {    //!alreadyupload
            if (mImageUri != null) {
                StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                        + "." + getFileExtension(mImageUri));

                mUploadTask = fileReference.putFile(mImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                /*int position=-1;
                                for(int i=0; i<mUploads.size(); i++){
                                    if((mUploads.get(i).getEmail()).equals(users_email)) {
                                        position = i;
                                    }
                                }
                                if(position!=-1)
                                    openDialog(position, users_email, "If you pick yes, your previous profile will be deleted!");

                                selectedItem = mUploads.get(position);*/


                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setProgress(0);
                                    }
                                }, 500);


                                Toast.makeText(HomeActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                                /*Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
                                        taskSnapshot.getStorage().getDownloadUrl().toString());
                                String uploadId = mDatabaseRef.push().getKey();
                                mDatabaseRef.child(uploadId).setValue(upload);*/
                                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!urlTask.isSuccessful()) ;
                                Uri downloadUrl = urlTask.getResult();

                                //Log.d(TAG, "onSuccess: firebase download url: " + downloadUrl.toString()); //use if testing...don't need this line.
                                Upload upload = new Upload(mEditTextFileName.getText().toString().trim(), downloadUrl.toString(), user.getEmail());

                                //if(selectedItem!=null) {
                                    upload.setCity(selectedItem.getCity());
                                    upload.setJob(selectedItem.getJob());
                                    upload.setUid(selectedItem.getUid());
                                //}
                                //HA A SHOWACTIVITYBEN TORLOM AKKOR ITT MAR NEM TUDOM MEGMENTENI


                                String uploadId = mDatabaseRef.push().getKey();
                                mDatabaseRef.child(uploadId).setValue(upload);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                mProgressBar.setProgress((int) progress);
                            }
                        });
                SharedPreferences.Editor editor = getSharedPreferences("uploads", MODE_PRIVATE).edit();
                editor.putBoolean(user.getEmail(), true);
                editor.apply();
                itsokey=0;
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImagesActivity() {
        Intent intent = new Intent(this, ShowActivity.class);
        startActivity(intent);
    }

    public void showprofile(View view){
        Intent i=new Intent(this, ProfileActivity.class);
        startActivity(i);
    }
    private boolean result;
    public boolean openDialog(final int position, final String users_mail, String information){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Delete");
        // Setting Dialog Message
        alertDialog.setMessage(information);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Yes, I'm sure", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                // Write your code here to invoke YES event
                deleteprofile(position, users_mail);
                result=true;
                dialog.dismiss();
            }
        });
        alertDialog.setIcon(R.drawable.delete);

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("No way", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to invoke NO event
                result=false;
                dialog.cancel();
            }
        });
        alertDialog.show();

        return result;
    }

    public void InformDialog(String information){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        // Setting Dialog Title
        alertDialog.setTitle("Delete");
        // Setting Dialog Message
        alertDialog.setMessage(information);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Coool", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                // Write your code here to invoke YES event
                //deleteprofile(position, users_mail);
                result=true;
                dialog.dismiss();
            }
        });
        alertDialog.setIcon(R.drawable.delete);

        // Setting Negative "NO" Button
        alertDialog.setCancelable(true);
        alertDialog.show();

    }

    public void deleteprofile(int position, final String users_email){
        selectedItem = mUploads.get(position);
        StorageReference imageref = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        final String selectedKey = selectedItem.getKey();
        imageref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(HomeActivity.this, "Your profile has been deleted!\nYou can upload now!", Toast.LENGTH_SHORT).show();
                HomeActivity.already = false;
                SharedPreferences.Editor editor = getSharedPreferences("uploads", MODE_PRIVATE).edit();
                editor.putBoolean(users_email, false);
                editor.apply();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (dlayout.isDrawerOpen(GravityCompat.START)) {
            dlayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()){
            case R.id.nav_myprofile:
                i = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(i);
                break;

            case R.id.nav_search:
                i = new Intent(HomeActivity.this, ShowActivity.class);
                startActivity(i);
                    break;

            case R.id.nav_chat:
                i = new Intent(HomeActivity.this, ConversationActivity.class);
                startActivity(i);
                break;
        }
        dlayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

/*manifest
android:theme="@style/AppTheme.SlidrActivityTheme" - profileacitivyben
 */