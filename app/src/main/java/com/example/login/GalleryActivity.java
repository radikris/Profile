package com.example.login;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.net.URI;
import java.net.URISyntaxException;

public class GalleryActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView description;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galery);
        Upload note = null;
        if(getIntent().hasExtra("selected_item")) {
            note = getIntent().getParcelableExtra("selected_item");
        }
        imageView=findViewById(R.id.image_note);
        description=findViewById(R.id.image_description);
        Uri myUri = Uri.parse(note.getImageUrl());
        Toast.makeText(this, note.getImageUrl(), Toast.LENGTH_SHORT).show();
        Picasso.get()
                .load(note.getImageUrl())
                .fit()
                .placeholder(R.drawable.loading)
                .centerCrop()
                //.resize(500, 400)
                .into(imageView);
        description.setText("Name: " + note.getName() + "\n" + "City: " + note.getCity() + "\n" + "Job: " +note.getJob() + "\n");

    }

}
