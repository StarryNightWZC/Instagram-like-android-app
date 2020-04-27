package com.example.logindemo;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MyViewAdapter extends RecyclerView.Adapter<MyViewAdapter.MyViewHolder> {

    //private Context mContext;
    private List<UploadFile> mUploads;
    private OnPicListener mOnPicListener;

    //public MyViewAdapter(Context context, List<UploadFile> uploads, OnPicListener onPicListener) {
    public MyViewAdapter(List<UploadFile> uploads, OnPicListener onPicListener) {

        //mContext = context;
        mUploads = uploads;
        this.mOnPicListener = onPicListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view, parent, false);
        return new MyViewHolder(view, mOnPicListener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

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

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imageView;
        OnPicListener onPicListener;
        public MyViewHolder(View itemView, OnPicListener onPicListener) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.singleImageView);
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
