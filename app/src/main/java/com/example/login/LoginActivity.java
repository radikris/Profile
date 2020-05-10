package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static int SPLASH_TIME_OUT=2000;

    EditText emailId, password;
    Button btnSignin;
    TextView tvSignup;
    FirebaseAuth mFirebaseAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mFirebaseAuth=FirebaseAuth.getInstance();
        emailId=findViewById(R.id.email);
        password=findViewById(R.id.pw);
        btnSignin=findViewById(R.id.signinbutton);
        tvSignup=findViewById(R.id.signup);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        mAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser=mFirebaseAuth.getCurrentUser();
                if( mFirebaseUser != null){
                    Toast.makeText(LoginActivity.this, "You are logged in!", Toast.LENGTH_LONG).show();
                    Intent i=new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(i);
                }else{
                    Toast.makeText(LoginActivity.this, "Please log in!", Toast.LENGTH_LONG).show();
                }
            }
        };

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailId.getText().toString();
                String pwd=password.getText().toString();
                if(email.isEmpty()){
                    emailId.setError("Please enter email!");
                    emailId.requestFocus();
                }
                else if(pwd.isEmpty()){
                    password.setError("Please enter your password!");
                    password.requestFocus();
                }
                else if(email.isEmpty() && pwd.isEmpty()){
                    emailId.setError("Please enter email!");
                    emailId.requestFocus();
                    password.setError("Please enter your password!");
                    password.requestFocus();
                    Toast.makeText(LoginActivity.this, "Fields are empty!", Toast.LENGTH_LONG).show();
                }
                else if(!(email.isEmpty() && pwd.isEmpty())){
                    progressBar.setVisibility(View.VISIBLE);
                    mFirebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Password or email incorrect! Try again please!", Toast.LENGTH_LONG).show();
                            }else{
                                progressBar.setVisibility(View.GONE);
                                Intent i=new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(i);
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(LoginActivity.this, "Something went wrong! Try again please!", Toast.LENGTH_LONG).show();
                }
            }
        });
        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }

    protected void onStart(){
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
