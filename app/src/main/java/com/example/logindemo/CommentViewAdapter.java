package com.example.logindemo;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentViewAdapter extends RecyclerView.Adapter<CommentViewAdapter.CommentViewHolder> {

    //private Context mContext;
    private List<UploadComment> mUploads;
    private OnPicListener mOnPicListener;

    //public CommentViewAdapter(Context context, List<UploadComment> uploads, OnPicListener onPicListener) {
    public CommentViewAdapter(List<UploadComment> uploads, OnPicListener onPicListener) {

        //mContext = context;
        mUploads = uploads;
        this.mOnPicListener = onPicListener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_comment, parent, false);
        return new CommentViewHolder(view, mOnPicListener);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {

        UploadComment uploadCurrent = mUploads.get(position);
        //holder.textViewName.setText(uploadCurrent.getName());
        //TODO change url to firebase
        Picasso.get()
                .load(uploadCurrent.getProfilePicRef())
                .fit()
                .centerCrop()
                .into(holder.imageView);
        //TODO change placeholder to firebase comment
        holder.comment.setText(uploadCurrent.getComment());
        holder.username.setText(uploadCurrent.getUsername());

        //.load(uploadCurrent.getProfilePicRef())
        //.load(R.drawable.profile)

        //holder.imageView.setImageResource(mUploads[position]);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //public ImageView imageView;
        public ImageView imageView;
        public TextView username;
        public TextView comment;
        OnPicListener onPicListener;
        public CommentViewHolder(View itemView, OnPicListener onPicListener) {
            super(itemView);
            //this.imageView = itemView.findViewById(R.id.singleImageView);
            this.imageView = itemView.findViewById(R.id.commentProfileImage);
            this.username = itemView.findViewById(R.id.usernameTextView);
            this.comment = itemView.findViewById(R.id.commentTextView);
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
