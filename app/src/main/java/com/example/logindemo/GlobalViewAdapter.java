package com.example.logindemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class GlobalViewAdapter extends RecyclerView.Adapter<GlobalViewAdapter.GlobalViewHolder> {

    //private Context mContext;
    private List<UploadFile> mUploads;
    private OnPicListener mOnPicListener;

    //public GlobalViewAdapter(Context context, List<UploadFile> uploads, OnPicListener onPicListener) {
    public GlobalViewAdapter(List<UploadFile> uploads, OnPicListener onPicListener) {

        //mContext = context;
        mUploads = uploads;
        this.mOnPicListener = onPicListener;
    }

    @NonNull
    @Override
    public GlobalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_global, parent, false);
        return new GlobalViewHolder(view, mOnPicListener);
    }

    @Override
    public void onBindViewHolder(GlobalViewHolder holder, int position) {

        UploadFile uploadCurrent = mUploads.get(position);
        //holder.textViewName.setText(uploadCurrent.getName());
        Picasso.get()
                .load(uploadCurrent.getStorageRef())
                .fit()
                .centerCrop()
                .into(holder.imageView);

        //holder.imageView.setImageResource(mUploads[position]);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class GlobalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imageView;
        OnPicListener onPicListener;
        public GlobalViewHolder(View itemView, OnPicListener onPicListener) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.singleGlobalImageView);
            this.onPicListener = onPicListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onPicListener.onPicClick(getAdapterPosition());
        }
    }

    public interface OnPicListener{
        void onPicClick(int position);
    }
}