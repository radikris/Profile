package com.example.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ImageViewHolder> implements Filterable {
    private Context mContext;
    private List<Upload> mUploads;
    private List<Upload> exampleListFull;
    private OnNoteListener mOnNoteListener;

    public ProfileAdapter(Context context, List<Upload> upload, OnNoteListener onNoteListener){
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
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Upload uploadCurrent = mUploads.get(position);
        holder.textViewName.setText(uploadCurrent.getName());
        Picasso.get()
                .load(uploadCurrent.getImageUrl())
                .fit()
                .placeholder(R.drawable.loading)
                //.centerCrop()
                //.resize(500, 400)
                .into(holder.imageView);

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

        public ImageViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
            imageView = itemView.findViewById(R.id.image_view_upload);
            this.onNoteListener=onNoteListener;

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
