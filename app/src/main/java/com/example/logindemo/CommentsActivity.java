package com.example.logindemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity implements CommentViewAdapter.OnPicListener{

    private static final String TAG = "CommentsActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore firestoreDB;
    private FirebaseStorage mStorage;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private UploadTask uploadTask;

    private List<UploadComment> mUploadsComment;
    private String userID;
    private String mCurrentPhotoPath;
    private String mPhotoPath;


    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter mAdapter;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private ProgressBar progressBar;
    Bitmap bitmapOriginal;
    Bitmap bitmapThumbNail;
    Uri mImageUri;
    private String timeStamp;
    private String username;
    private File storageDir;

    private ImageView commentPhoto;
    private String commentPhotoUrl;
    private String commentPhotoCaption;
    private String photoID;

    private Uri profilePicUrl;

    private TextInputLayout commentEditText;

    private String pictureOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userID = mUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);

        Intent incomingIntent = getIntent();
        commentPhotoUrl = incomingIntent.getStringExtra("CommentPhotoUrl");
        commentPhotoCaption = incomingIntent.getStringExtra("CommentPhotoCaption");
        photoID = incomingIntent.getStringExtra("PhotoID");

        TextView captionTextView = findViewById(R.id.caption_TextView);
        captionTextView.setText(commentPhotoCaption);

        commentEditText = findViewById(R.id.EditTextComment);
        Button postCommentBtn = findViewById(R.id.BtnComment);
        Button deletePic = findViewById(R.id.BtnDelete);

        mUploadsComment = new ArrayList<>();

        recyclerView = findViewById(R.id.commentRecyclerView);
        layoutManager = new GridLayoutManager(CommentsActivity.this, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        mAdapter = new CommentViewAdapter(mUploadsComment,this);
        recyclerView.setAdapter(mAdapter);

        commentPhoto = findViewById(R.id.photo_image);

        if (commentPhotoUrl != null){
            Picasso.get().load(commentPhotoUrl).fit().centerCrop().into(commentPhoto);
        }

        //get current username
        firestoreDB.collection("Users").document(userID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Userprofile userprofile = documentSnapshot.toObject(Userprofile.class);
                        username = userprofile.getname();
                    }
                });

        postCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = commentEditText.getEditText().getText().toString().trim();
                validateComment();
                uploadComments(comment);
            }
        });
        deletePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePicture();
            }
        });
        loadComments();
    }

    private void uploadComments(String comment){
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String comment_final = comment;
        progressBar.setVisibility(View.VISIBLE);
        //get downloadurl for profile picture
        mStorage.getReference("uploads").child(userID).child("displayPic.jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                UploadComment upload = new UploadComment(uri.toString(), timeStamp, comment_final, username);
                firestoreDB.collection("Photos")
                        .document(photoID).collection("Comments").add(upload);
                Toast.makeText(CommentsActivity.this, "Commnet Posted!" ,Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    private void loadComments() {
        //realtime update
        Query commentTimeOrderDescend = firestoreDB.collection("Photos").document(photoID)
                .collection("Comments").orderBy("timeStamp", Query.Direction.DESCENDING);
        commentTimeOrderDescend.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                    return;
                }else{
                    mUploadsComment.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()){
                        UploadComment userComment = document.toObject(UploadComment.class);
                        mUploadsComment.add(userComment);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean validateComment() {
        String comment = commentEditText.getEditText().getText().toString().trim();

        if (comment.isEmpty()) {
            commentEditText.setError("Field can't be empty");
            return false;
        } else {
            commentEditText.setError(null);
            return true;
        }
    }

    private void deletePicture(){
        //TODO get value out of inner class
        DocumentReference docRef = firestoreDB.collection("Photos").document(photoID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UploadFile uploadFile = documentSnapshot.toObject(UploadFile.class);
                String pictureOwner = uploadFile.getUserId();
                if(pictureOwner.equals(userID)){
                    deletePictureFirebase();
                    StorageReference imageRef = mStorage.getReferenceFromUrl(uploadFile.getStorageRef());
                    imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(CommentsActivity.this, "Picture successfully deleted!", Toast.LENGTH_SHORT).show();
                            firestoreDB.collection("Photos").document(photoID).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CommentsActivity.this, "Database entry successfully deleted!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(CommentsActivity.this, SignedInActivity.class));
                                        }
                                    });
                        }
                    });
                }else{
                    Toast.makeText(CommentsActivity.this, "Only owner can delete this picture!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void deletePictureFirebase(){

    }

    @Override
    public void onPicClick(int position) {

    }
}
