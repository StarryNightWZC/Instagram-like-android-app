package com.example.logindemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import de.hdodenhof.circleimageview.CircleImageView;

public class SignedInActivity extends AppCompatActivity implements MyViewAdapter.OnPicListener {

    private static final String TAG = "SignedInActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore firestoreDB;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private UploadTask uploadTask;

    private List<UploadFile> mUploads;
    private String userID;
    private String mCurrentPhotoPath;
    private String mPhotoPath;

    private TextView username;
    private TextView bio;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter mAdapter;

    Bitmap bitmap;
    CircleImageView Profile;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private ProgressBar progressBar;
    Bitmap bitmapOriginal;
    Bitmap bitmapThumbNail;
    Uri mImageUri;
    String timeStamp;
    private File storageDir;

    private final TreeMap<String,UploadFile> Picinfo=new TreeMap<>(Collections.reverseOrder());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);

        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        Profile = findViewById(R.id.profile_image);
        progressBar = findViewById(R.id.progressBar);

        FloatingActionButton addPhoto = findViewById(R.id.buttonAddPhoto);
        FloatingActionButton logOut = findViewById(R.id.buttonLogOut);
        FloatingActionButton global = findViewById(R.id.buttonGlobal);

        mUploads = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(SignedInActivity.this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        mAdapter = new MyViewAdapter(mUploads,this);
        recyclerView.setAdapter(mAdapter);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userID = mUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        firestoreDB = FirebaseFirestore.getInstance();

        if (mUser != null) {
            userID = mUser.getUid();
            loadProfile(userID);
        }else{
            Toast.makeText(SignedInActivity.this, "Authentication Failed." ,Toast.LENGTH_LONG).show();
        }

        Intent incomingIntent = getIntent();
        bitmap = incomingIntent.getParcelableExtra("ProfilePic");
        if (bitmap != null){
            Profile.setImageBitmap(bitmap);
        }else{
            final StorageReference fileReference = mStorageRef.child(userID).child("displayPic.jpg");
            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Picasso.get()
                            .load(uri)
                            .fit()
                            .centerCrop()
                            .into(Profile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        loadGallery();
        Log.i(TAG, "onCreate");
    }

    /*private void loadGallery() {
        final MyViewAdapter.OnPicListener onPicListener = this;

        firestoreDB.collection("Photos").document(userID).collection("photos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(QueryDocumentSnapshot document:queryDocumentSnapshots){
                    mUploads.clear();
                    if(document.exists()){
                        UploadFile userpicture=document.toObject(UploadFile.class);
                        if(Picinfo.containsKey(userpicture.timeStamp)){

                        }else{
                            Picinfo.put(userpicture.timeStamp,userpicture);
                        }
                    }else {
                        Toast.makeText(SignedInActivity.this, "No such Picture document", Toast.LENGTH_SHORT).show();
                    }
                }
                Iterator<Map.Entry<String,UploadFile>> io=Picinfo.entrySet().iterator();
                while (io.hasNext()){
                    Map.Entry<String,UploadFile> me=io.next();
                    final String key=(String)me.getKey();
                    final UploadFile value=me.getValue();
                    mUploads.add(value);
                    mAdapter = new MyViewAdapter(mUploads,onPicListener);
                    recyclerView.setAdapter(mAdapter);

                }
            }
        });
    }*/

    private void loadGallery() {
        //TODO photolist doesn't update coming back from photocaption
        //.whereEqualTo("userId", userID)
        //Toast.makeText(SignedInActivity.this, "LOAD GALLERY!" ,Toast.LENGTH_SHORT).show();

        Query photoTimeOrderDescend = firestoreDB.collection("Photos")
                .whereEqualTo("userId", userID).orderBy("timeStamp", Query.Direction.DESCENDING);
        /*photoTimeOrderDescend.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    mUploads.clear();
                    for (QueryDocumentSnapshot document : task.getResult()){
                        UploadFile userPhoto = document.toObject(UploadFile.class);
                        mUploads.add(userPhoto);
                    }
                }else{
                    Log.d(TAG, "Error getting documents:", task.getException());
                }
                mAdapter = new MyViewAdapter(mUploads,onPicListener);
                recyclerView.setAdapter(mAdapter);
            }
        });*/

        //realtime update
        photoTimeOrderDescend.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                    return;
                }else{
                    mUploads.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()){
                        UploadFile userPhoto = document.toObject(UploadFile.class);
                        mUploads.add(userPhoto);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        progressBar.setVisibility(View.GONE);
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        Log.i(TAG, "onResume");
    }

    public void onLogout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, MainActivity.class));
    }

    public void onGlobal(View view) {
        startActivity(new Intent(this, GlobalActivity.class));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            bitmapThumbNail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mCurrentPhotoPath), 300, 300);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.outWidth = 1024;
            bmOptions.outHeight = 1024;
            bitmapOriginal = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

            String fname = timeStamp +".jpg";

            File image = new File(storageDir, fname);
            if (image.exists()) image.delete ();
            try {
                FileOutputStream out = new FileOutputStream(image);
                bitmapOriginal.compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mPhotoPath = image.getAbsolutePath();
            mImageUri = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    image);
            Intent intent = new Intent(SignedInActivity.this, PhotoCaptionActivity.class);
            intent.putExtra("PhotoUrl", mImageUri);
            intent.putExtra("PhotoBitmap", bitmapThumbNail);
            startActivity(intent);
            //changed from upload to firebase into photocaption activity
            //firebaseStore();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(SignedInActivity.this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /*public void firebaseStore(){
        mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            userID = mUser.getUid();
        }else{
            Toast.makeText(SignedInActivity.this, "Authentication Failed." ,Toast.LENGTH_LONG).show();
        }
        uploadFile();
    }

    private void uploadFile() {
        progressBar.setVisibility(View.VISIBLE);
        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(userID).child(timeStamp + ".jpg");
            uploadTask = fileReference.putFile(mImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        Uri downloadUri = task.getResult();
                        UploadFile upload = new UploadFile(downloadUri.toString(), timeStamp);
                        firestoreDB.collection("Photos").document(userID).collection("photos").add(upload);
                        loadGallery();
                    } else {
                        // Handle failures
                        Toast.makeText(SignedInActivity.this, "Failed to get photo downloadUrl.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }*/

    private void loadProfile(String userID){
        progressBar.setVisibility(View.VISIBLE);
        firestoreDB.collection("Users").document(userID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            DocumentSnapshot document=task.getResult();
                            if(document.exists()){
                                Userprofile profile=document.toObject(Userprofile.class);
                                username.setText(profile.getname());
                                bio.setText(profile.getbio());
                            }else{
                                Toast.makeText(SignedInActivity.this, "No match!", Toast.LENGTH_LONG).show();
                            }
                        }else{
                            Toast.makeText(SignedInActivity.this, "Failed!"+task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPicClick(int position) {
        //click thumbnail for full screen view
        ImageView imageView = new ImageView(SignedInActivity.this);
        final AlertDialog dialog=new AlertDialog.Builder(SignedInActivity.this).create();
        Picasso.get().load(mUploads.get(position).getStorageRef()).into(imageView);
        dialog.setView(imageView);
        dialog.show();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        /*String commentPhotoUrl = mUploads.get(position).getStorageRef();
        String commentPhotoCaption = mUploads.get(position).getCaption();
        String photoId = mUploads.get(position).getPhotoId();
        Intent intent = new Intent(SignedInActivity.this, CommentsActivity.class);
        intent.putExtra("CommentPhotoUrl", commentPhotoUrl);
        intent.putExtra("CommentPhotoCaption", commentPhotoCaption);
        intent.putExtra("PhotoID", photoId);
        startActivity(intent);*/
    }
}
