package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

public class MainActivity extends AppCompatActivity {

    EditText emailId, password, city, job, username;
    Button btnSignup;
    TextView tvSignin;
    FirebaseAuth mFirebaseAuth;
    private ProgressBar progressBar;
    private Uri mImageUri;
    ImageView mImageView;

    StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;

    private Button mButtonChooseImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailId = findViewById(R.id.email);
        password = findViewById(R.id.pw);
        city = findViewById(R.id.city);
        job = findViewById(R.id.job);
        username = findViewById(R.id.username);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        mImageUri = Uri.parse("android.resource://com.example.login/drawable/gotoprofile");
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mImageView = findViewById(R.id.image_view);
        /*mButtonChooseImage = findViewById(R.id.button3);
        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });*/


        btnSignup = findViewById(R.id.singupbutton);
        tvSignin = findViewById(R.id.signin);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailId.getText().toString();
                final String pwd = password.getText().toString();
                final String mcity = city.getText().toString();
                final String mjob = job.getText().toString();
                final String mname = username.getText().toString();
                if (email.isEmpty()) {
                    emailId.setError("Please enter email!");
                    emailId.requestFocus();
                } else if (pwd.isEmpty()) {
                    password.setError("Please enter your password!");
                    password.requestFocus();
                } else if (pwd.length() < 6) {
                    password.setError("Too short!-(Required: at least 6!)");
                    password.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailId.setError("Please enter a valid email!");
                    emailId.requestFocus();
                }
                /*else if(!checkEmail(emailId.getText().toString())){
                    emailId.setError("This email is already registered!");
                    emailId.requestFocus();
                }*/
                else if (email.isEmpty() && pwd.isEmpty()) {
                    emailId.setError("Please enter email!");
                    emailId.requestFocus();
                    password.setError("Please enter your password!");
                    password.requestFocus();
                    Toast.makeText(MainActivity.this, "Fields are empty!", Toast.LENGTH_LONG).show();
                } else if (mcity.isEmpty()) {
                    city.setError("Please enter your City");
                    city.requestFocus();
                } else if (mname.isEmpty()) {
                    username.setError("Please enter your Name");
                    username.requestFocus();
                } else if (mjob.isEmpty()) {
                    job.setError("Please enter your Job");
                    job.requestFocus();
                } else if (!(email.isEmpty() && pwd.isEmpty())) {
                    progressBar.setVisibility(View.VISIBLE);
                    mFirebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Upps, Registration failed! Maybe this email already used?!", Toast.LENGTH_LONG).show();
                            } else {
                                StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                                        + "." + getFileExtension(mImageUri));
                                /**/

                                //ide kell a kepes
                                mUploadTask = fileReference.putFile(mImageUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                    }
                                                }, 2000);

                                                Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                                                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                                while (!urlTask.isSuccessful()) ;
                                                Uri downloadUrl = urlTask.getResult();
                                                //Log.d(TAG, "onSuccess: firebase download url: " + downloadUrl.toString()); //use if testing...don't need this line.

                                                User user = new User(mname, mcity, mjob, email, downloadUrl.toString());
                                                user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());

                                                /*String uploadId = mDatabaseRef.push().getKey();
                                                mDatabaseRef.child(uploadId).setValue(user);*/

                                                FirebaseDatabase.getInstance().getReference("uploads")
                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                progressBar.setVisibility(View.GONE);
                                                                if (task.isSuccessful()) {
                                                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                                    Toast.makeText(MainActivity.this, "Great, Succesful registration!", Toast.LENGTH_LONG).show();
                                                                }
                                                            }   //ONCOMPLETE
                                                        }   //ONCOMPLETELISTENER
                                                );


                                                /*Upload upload = new Upload(mEditTextFileName.getText().toString().trim(), downloadUrl.toString(), user.getEmail());

                                                String uploadId = mDatabaseRef.push().getKey();
                                                mDatabaseRef.child(uploadId).setValue(upload);*/
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                /*
                                /*User user = new User(mname, mcity, mjob, email, "alma");
                                Toast.makeText(MainActivity.this, mImageUri.toString(), Toast.LENGTH_LONG).show();
                                FirebaseDatabase.getInstance().getReference("uploads")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(
                                        new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressBar.setVisibility(View.GONE);
                                                if (task.isSuccessful()) {
                                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                    Toast.makeText(MainActivity.this, "Great, Succesful registration!", Toast.LENGTH_LONG).show();
                                                }
                                            }   //ONCOMPLETE
                                        }   //ONCOMPLETELISTENER
                                );*/


                            }//ELSE VEGE


                        }   //); //itt van vege az ujnak
                    });

                }       //ez itt az else if vege
                else {
                    Toast.makeText(MainActivity.this, "Something went wrong! Try again please!", Toast.LENGTH_LONG).show();
                }
            }
        });
        tvSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }

    public boolean checkEmail(String myemail) {
        final boolean[] email_empty = new boolean[1];
        email_empty[0] = false;
        mFirebaseAuth.fetchSignInMethodsForEmail(myemail).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                email_empty[0] = task.getResult().getSignInMethods().isEmpty();
            }
        });
        return email_empty[0];
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /*private void openFileChooser() {
        CropImage.activity().start(MainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                Picasso.get()
                        .load(mImageUri)
                        .resize(500, 400)
                        .into(mImageView);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception e = result.getError();
                Toast.makeText(MainActivity.this, "Something went wrong, maybe " + e, Toast.LENGTH_SHORT).show();
            }
        }
    }*/
}
