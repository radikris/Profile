package com.example.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.example.login.HomeActivity;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

public class ShowActivity extends AppCompatActivity implements ProfileAdapter.OnNoteListener {

    private RecyclerView mRecyclerView;
    private ProfileAdapter mAdapter;

    private ProgressBar mProgressCircle;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference mDatabaseRef_ratings;
    private List<Upload> mUploads;
    public ArrayList<Upload> filteredList;
    public ArrayList<Ratingsclass> ratingslist;

    private ValueEventListener mDBListener;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseStorage mStorage;

    private EditText thefilter;
    private Spinner spinner1;
    private Spinner spinner2;
    private Spinner spinner3;

    public String name_item_spinner;
    public String city_item_spinner;
    public String job_item_spinner;
    public String mixed_spinner;
    public String star_emoji;
    public boolean isfiltered;
    public float sumofstars, numofratings, defaultstars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Slidr.attach(this);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d("myTag", "oncreate eddig oke");

        int unicode=0x2B50;
        star_emoji=getEmoji(unicode);

        mUploads = new ArrayList<>();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mDatabaseRef_ratings = FirebaseDatabase.getInstance().getReference("ratings");

        mProgressCircle=findViewById(R.id.progress_cirle);
        mStorage = FirebaseStorage.getInstance();

        isfiltered=false;

        final EditText thefilter=(EditText)findViewById(R.id.searchfilter);
        spinner1=findViewById(R.id.spinner1);
        spinner2=findViewById(R.id.spinner2);
        spinner3=findViewById(R.id.spinner3);
        final List<String> name_spinner=new ArrayList<>();
        final List<String> city_spinner=new ArrayList<>();
        final List<String> job_spinner=new ArrayList<>();
        name_spinner.add(0, "Rating");
        city_spinner.add(0, "City");
        job_spinner.add(0, "Job");
        final List<String>all_spinner=new ArrayList<>();

        ArrayAdapter<String> dataAdapter;
        dataAdapter= new ArrayAdapter(this, android.R.layout.simple_spinner_item, name_spinner);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(dataAdapter);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).equals("Rating")){
                    //thefilter.getText().clear();
                    for(int i=0; i<name_spinner.size(); i++){
                        if(all_spinner.contains(name_spinner.get(i))){
                            all_spinner.remove(name_spinner.get(i));
                            //mixed_spinner.replace(name_spinner.get(i), "Name");
                        }
                    }
                    all_spinner.add("Rating");
                    mixed_spinner = String.join(",", all_spinner);
                    if(isfiltered)
                        thefilter.setText(mixed_spinner);
                }else{
                    name_item_spinner=parent.getItemAtPosition(position).toString();
                    for(int i=0; i<name_spinner.size(); i++){
                        if(all_spinner.contains(name_spinner.get(i))){
                            all_spinner.remove(name_spinner.get(i));
                        }
                    }
                    all_spinner.add(name_item_spinner);
                    mixed_spinner = String.join(",", all_spinner);
                    thefilter.setText(mixed_spinner);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dataAdapter= new ArrayAdapter(this, android.R.layout.simple_spinner_item, city_spinner);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(dataAdapter);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).equals("City")){
                    //thefilter.getText().clear();
                    for(int i=0; i<city_spinner.size(); i++){
                        if(all_spinner.contains(city_spinner.get(i))){
                            all_spinner.remove(city_spinner.get(i));
                            //mixed_spinner.replace(city_spinner.get(i), "City");     //tovabbra is marad az elozoleg beallitott
                        }
                    }
                    all_spinner.add("City");
                    mixed_spinner = String.join(",", all_spinner);
                    if(isfiltered)
                        thefilter.setText(mixed_spinner);
                    //all_spinner.add("City");
                }else{
                    city_item_spinner=parent.getItemAtPosition(position).toString();//+",";
                    for(int i=0; i<city_spinner.size(); i++){
                        if(all_spinner.contains(city_spinner.get(i))){
                            all_spinner.remove(city_spinner.get(i));
                        }
                    }
                    all_spinner.add(city_item_spinner);
                    mixed_spinner = String.join(",", all_spinner);
                    thefilter.setText(mixed_spinner);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(ShowActivity.this, "nothing city", Toast.LENGTH_LONG).show();
            }
        });

        dataAdapter= new ArrayAdapter(this, android.R.layout.simple_spinner_item, job_spinner);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(dataAdapter);
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).equals("Job")){
                    //thefilter.getText().clear();
                    for(int i=0; i<job_spinner.size(); i++){
                        if(all_spinner.contains(job_spinner.get(i))){
                            all_spinner.remove(job_spinner.get(i));
                        }
                    }
                    all_spinner.add("Job");
                    mixed_spinner = String.join(",", all_spinner);
                    if(isfiltered)
                        thefilter.setText(mixed_spinner);
                    //all_spinner.add("Job");
                }else{
                    job_item_spinner=parent.getItemAtPosition(position).toString();//+",";
                    for(int i=0; i<job_spinner.size(); i++){
                        if(all_spinner.contains(job_spinner.get(i))){
                            all_spinner.remove(job_spinner.get(i));
                        }
                    }
                    all_spinner.add(job_item_spinner);
                    mixed_spinner = String.join(",", all_spinner);
                    thefilter.setText(mixed_spinner);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Log.d("myTag", "eloszor az uploads");

        mDBListener= mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mAuth = FirebaseAuth.getInstance();
                final FirebaseUser user = mAuth.getCurrentUser();
                String users_email = user.getEmail();

                mUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setEmail(postSnapshot.getValue(Upload.class).getEmail());
                    upload.setKey(postSnapshot.getKey());
                    User nupload = postSnapshot.getValue(User.class);
                    upload.setCity(nupload.getCity());
                    upload.setJob(nupload.getJob());
                    upload.setImageUrl(nupload.getImageUrl());
                    mUploads.add(upload);
                }
                //Toast.makeText(ShowActivity.this, mUploads.get(0).getName()+" " + mUploads.get(0).getCity(), Toast.LENGTH_SHORT).show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // yourMethod();
                    }
                }, 1000);   //5 seconds*/
                /*ratingbar???

                 */
                //Log.d("myTag", "most jon a ratings");

                Log.d("myTag", "most jon az adapter");
                mAdapter = new ProfileAdapter(ShowActivity.this, mUploads, ShowActivity.this);//, ratingslist); //nekem sima this nem jo
                mAdapter.notifyDataSetChanged();
                mRecyclerView.setAdapter(mAdapter);
                mProgressCircle.setVisibility(View.INVISIBLE);

                name_spinner_setup(name_spinner);
                city_spinner_setup(city_spinner);
                job_spinner_setup(job_spinner);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ShowActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });

        //function();


        thefilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            //@Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        /*itt van meg hely onreatben*/


    }

    void name_spinner_setup(List<String>name_spinner){
        /*for(Upload item: mUploads){
            if(name_spinner.contains(item.getName()))
                continue;
            else
                name_spinner.add(item.getName());
        }*/
        for(int i=5; i>0; i--){
            name_spinner.add(i+star_emoji);
        }
    }

    void city_spinner_setup(List<String>city_spinner){
        for(Upload item: mUploads){
            if(city_spinner.contains(item.getCity()))
                continue;
            else
                city_spinner.add(item.getCity());
        }
    }

    void job_spinner_setup(List<String>name_spinner){
        for(Upload item: mUploads){
            if(name_spinner.contains(item.getJob()))
                continue;
            else
                name_spinner.add(item.getJob());
        }
    }

    void function(){
        Log.d("myTag", "function called");
        for (int i = 0; i < mUploads.size(); i++){
            mDatabaseRef_ratings = FirebaseDatabase.getInstance().getReference("ratings");
            Log.d("myTag", "loop");
            //checking if child already exists
            mDatabaseRef_ratings=mDatabaseRef_ratings.child(mUploads.get(i).getKey());
            Log.d("myTag", "ez az id: "+ mUploads.get(i).getKey());

            final int finalI = i;
            mDatabaseRef_ratings.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        // The child doesn't exist
                        Log.d("myTag", "has no rating");
                    }else{
                        mDatabaseRef_ratings = FirebaseDatabase.getInstance().getReference("ratings").child(mUploads.get(finalI).getKey());
                        Log.d("myTag", "checking the elements of this child");
                        mDatabaseRef_ratings.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Ratingsclass temp = new Ratingsclass();
                                numofratings = 0;
                                sumofstars = 0;
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    temp = postSnapshot.getValue(Ratingsclass.class);
                                    //temp.setRatingid(postSnapshot.getKey());
                                    numofratings += 1;
                                    sumofstars += temp.getNum_of_stars();
                                    Log.d("myTag", numofratings + " es " + sumofstars);
                                }
                                if (numofratings == 0)
                                    defaultstars = 0;
                                else
                                    defaultstars = sumofstars / numofratings;
                                temp.setRatingid(mUploads.get(finalI).getKey());
                                temp.setRating_sum(defaultstars);
                                ratingslist.add(temp);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //
                }
            });

        }

    }

    private void filter(String text) {
        /*ArrayList<Upload> */filteredList = new ArrayList<>();

        List<String> myList = new ArrayList<String>(Arrays.asList(text.split(",")));
        myList.remove("Rating");
        myList.remove("City");
        myList.remove("Job");

        text.replace("Rating", "-");
        text.replace("City", "-");
        text.replace("Job", "-");

        if(!(text.contains("Rating") && text.contains("City") && text.contains("Job"))){
            Toast.makeText(ShowActivity.this, text, Toast.LENGTH_LONG).show();
            for (Upload item : mUploads) {
                int match_counter = 0;
                for (int i = 0; i < myList.size(); i++) {
                    if (String.valueOf(item.getRating()).equals(myList.get(i).substring(0, 1))) {
                        match_counter += 1;
                    }
                    if (item.getCity().toLowerCase().contains(myList.get(i).toLowerCase())) {
                        match_counter += 1;
                    }
                    if (item.getJob().toLowerCase().contains(myList.get(i).toLowerCase())) {
                        match_counter += 1;
                    }
                    if (!filteredList.contains(item) && match_counter == myList.size())
                        filteredList.add(item);
                }
            }
        }else{
            isfiltered=false;
            mAdapter.filterList((ArrayList<Upload>) mUploads);
            return;
        }

        text="";
        isfiltered=true;
        if(filteredList.size()==0)
            Toast.makeText(ShowActivity.this, "No result found:(", Toast.LENGTH_LONG).show();
        if(myList.size()==0)
            mAdapter.filterList(null);
        else
            mAdapter.filterList(filteredList);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }

    public void DeleteButton(View view) throws InterruptedException {
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        String users_email=user.getEmail();
        int position=-1;
        for(int i=0; i<mUploads.size(); i++){
            if((mUploads.get(i).getEmail()).equals(users_email)) {
                position = i;
            }
            //Toast.makeText(ShowActivity.this, mUploads.get(i).getEmail().toString(), Toast.LENGTH_SHORT).show();
        }
        if(position!=-1){
            openDialog(position, users_email);
        }else{
            Toast.makeText(ShowActivity.this, "You don't have any profile yet!", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteprofile(int position, final String user_mail){
        Upload selectedItem = mUploads.get(position);
        Toast.makeText(ShowActivity.this, selectedItem.getCity()+selectedItem.getImageUrl(), Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
            }
        }, 1000);

        StorageReference imageref = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        final String selectedKey = selectedItem.getKey();
        imageref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(ShowActivity.this, "Your profile has been deleted!", Toast.LENGTH_SHORT).show();
                HomeActivity.already = false;
                SharedPreferences.Editor editor = getSharedPreferences("uploads", MODE_PRIVATE).edit();
                editor.putBoolean(user_mail, false);
                editor.apply();
            }
        });
    }

    public static boolean result;
    public void openDialog(final int position, final String users_mail){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShowActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Delete");
        // Setting Dialog Message
        alertDialog.setMessage("Are you sure you want to delete your profile?");

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Yes, I'm sure", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                // Write your code here to invoke YES event
                deleteprofile(position, users_mail);
                dialog.dismiss();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("No way", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to invoke NO event
                dialog.cancel();
            }
        });
        alertDialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        //SearchView searchView = (SearchView) searchItem.getActionView();  HA HIBA EZT NEZZUK MEG
        //androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        //SearchView searchView = (SearchView)searchItem.getActionView();
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onNoteClick(int position) {
        Intent intent=new Intent(this, GalleryActivity.class);
        if(isfiltered)
            intent.putExtra("selected_item", filteredList.get(position));
        else
            intent.putExtra("selected_item", mUploads.get(position));
        startActivity(intent);
    }

    /*@Override
    public void onNoteClick(int position){
        mUploads.get(position);
        Intent intent=new Intent(this, ShowProfile.class);
    }*/
    public String getEmoji(int uni)
    {
        return new String(Character.toChars(uni));
    }


}
