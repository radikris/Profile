package com.example.login;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ImageViewHolder> implements Filterable {
    private Context mContext;
    private List<Upload> mUploads;
    private List<Upload> exampleListFull;
    private OnNoteListener mOnNoteListener;
    private ArrayList<Ratingsclass> mratings_array;
    public float sumofstars, numofratings, defaultstars;
    private DatabaseReference mDatabaseRef_ratings;
    public boolean alreadyrated;
    public Map<String, Float> ratings_map;

    public ProfileAdapter(Context context, List<Upload> upload, OnNoteListener onNoteListener){//, ArrayList<Ratingsclass> ratinglist){
        mContext=context;
        mUploads=upload;
        exampleListFull=new ArrayList<>(upload);
        mOnNoteListener=onNoteListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v,mOnNoteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {
        Upload uploadCurrent = mUploads.get(position);
        holder.textViewName.setText(uploadCurrent.getName());
        Picasso.get()
                .load(uploadCurrent.getImageUrl())
                .fit()
                .placeholder(R.drawable.loading)
                .centerCrop()
                //.resize(500, 400)
                .into(holder.imageView);

        holder.ratingBar.setRating(0) ;
        ratings_map=new HashMap<>();
        mratings_array=new ArrayList<Ratingsclass>();
        mDatabaseRef_ratings = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef_ratings.child("ratings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                    alreadyrated=false;
                    for (DataSnapshot propertySnapshot : entrySnapshot.getChildren()) {
                        for(int i=0; i<mUploads.size(); i++){
                            if(mUploads.get(i).getKey().equals(entrySnapshot.getKey())) {
                                alreadyrated = true;
                                break;
                            }
                        }
                        if(alreadyrated){
                            mDatabaseRef_ratings = FirebaseDatabase.getInstance().getReference("ratings").child(entrySnapshot.getKey());
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
                                    }
                                    if (numofratings == 0)
                                        defaultstars = 0;
                                    else
                                        defaultstars = (float)(sumofstars / numofratings);
                                    temp.setRatingid(entrySnapshot.getKey());
                                    temp.setRating_sum(defaultstars);
                                    ratings_map.put(entrySnapshot.getKey(), defaultstars);
                                    mratings_array.add(temp);
                                    /*for (int i = 0; i < mratings_array.size(); i++) {           //ez nem itt
                                        if (mratings_array.get(i).getRatingid().equals(mUploads.get(position).getKey())) {
                                            //holder.ratingBar.setRating(mratings_array.get(i).getRating_sum());
                                            holder.ratingBar.setRating(ratings_map.get(mratings_array.get(i).getRatingid()));
                                            Log.d("myTag", "ratinget beallitjuk: " + ratings_map.get(mratings_array.get(i).getRatingid() + mratings_array.get(i).getRatingid()));
                                        }else{
                                            holder.ratingBar.setRating((float)0.0);
                                        }
                                    }*/

                                    for(String key : ratings_map.keySet()){
                                        if(position < mUploads.size() && key.equals(mUploads.get(position).getKey())){
                                            //holder.ratingBar.setRating(ratings_map.get(key));
                                            holder.ratingBar.setRating(ratings_map.get(key));
                                            mUploads.get(position).setRating((int) Math.ceil(ratings_map.get(key)));
                                            Log.d("myTag", "ratingben->"+ ratings_map.get(key));
                                        }
                                    }

                                    /*for (String key : ratings_map.keySet()) {
                                        Log.d("myTag", "key a mapben: " + key);
                                        Log.d("myTag", "simaban->"+ ratings_map.get(key));
                                    }*/


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

    @Override
    public int getItemCount() {
        if(mUploads==null)
            return 0;
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewName;
        ImageView imageView;
        OnNoteListener onNoteListener;
        RatingBar ratingBar;

        public ImageViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
            imageView = itemView.findViewById(R.id.image_view_upload);
            this.onNoteListener=onNoteListener;
            ratingBar=itemView.findViewById(R.id.my_ratingBar);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onNoteListener.onNoteClick(getAdapterPosition());
        }
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Upload> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(exampleListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Upload item : exampleListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mUploads.clear();
            mUploads.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public void filterList(ArrayList<Upload> filteredList) {
        mUploads = filteredList;
        notifyDataSetChanged();
    }

    public interface OnNoteListener{
        void onNoteClick(int position);
    }

}
