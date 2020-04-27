package com.example.logindemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PhotoCaptionActivity extends AppCompatActivity {

    private static final String TAG = "PhotoCaptionActivity";

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

    private Bitmap bitmap;
    CircleImageView Profile;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private ProgressBar progressBar;
    Bitmap bitmapOriginal;
    Bitmap bitmapThumbNail;
    String timeStamp;
    private File storageDir;

    private ImageView commentPhoto;
    private String commentPhotoUrl;
    private Uri captionPhotoUri;
    private EditText captionEditText;
    private String captionNoHashTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_caption);

        progressBar = findViewById(R.id.progressBar);

        mUploads = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userID = mUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        firestoreDB = FirebaseFirestore.getInstance();

        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        Intent incomingIntent = getIntent();
        captionPhotoUri = incomingIntent.getParcelableExtra("PhotoUrl");
        bitmap = incomingIntent.getParcelableExtra("PhotoBitmap");

        Button postButton = findViewById(R.id.BtnPost);
        Button cancelButton = findViewById(R.id.BtnCancel);
        Switch swithHashTag = findViewById(R.id.SwitchHashTag);
        ImageView imageView = findViewById(R.id.photo_image);
        captionEditText = findViewById(R.id.captionEditText);

        //Picasso.get().load(captionPhotoUri).fit().centerCrop().into(imageView);
        imageView.setImageBitmap(bitmap);

        //TODO setup captionPhotoUri and upload photo and caption to firebase
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseStore();
                Intent intent = new Intent(PhotoCaptionActivity.this, SignedInActivity.class);
                //intent.putExtra("captionPhotoUri", captionPhotoUri);
                startActivity(intent);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhotoCaptionActivity.this, SignedInActivity.class);
                //intent.putExtra("captionPhotoUri", captionPhotoUri);
                startActivity(intent);
            }
        });
        swithHashTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.d(TAG, "Auto HashTag Enabled");
                    if(bitmap==null){
                        finish();
                    }
                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                    FirebaseVisionOnDeviceImageLabelerOptions options = new FirebaseVisionOnDeviceImageLabelerOptions
                            .Builder().setConfidenceThreshold(0.7f).build();
                    FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options);
                    labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                            Log.d(TAG, "Label successfully added");
                            captionNoHashTag = captionEditText.getText().toString();
                            for (FirebaseVisionImageLabel label: firebaseVisionImageLabels){
                                String text = label.getText();
                                captionEditText.append("#"+text+" ");
                            }
                            Toast.makeText(PhotoCaptionActivity.this, "Auto Hashtag enabled!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Failed to create label");
                        }
                    });
                }else{
                    captionEditText.setText(captionNoHashTag);
                    Toast.makeText(PhotoCaptionActivity.this, "Auto Hashtag disabled!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void firebaseStore(){
        mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            userID = mUser.getUid();
        }else{
            Toast.makeText(PhotoCaptionActivity.this, "Authentication Failed." ,Toast.LENGTH_LONG).show();
        }
        uploadFile();
    }

    private void uploadFile() {
        progressBar.setVisibility(View.VISIBLE);
        if (captionPhotoUri != null) {
            final StorageReference fileReference = mStorageRef.child(userID).child(timeStamp + ".jpg");
            uploadTask = fileReference.putFile(captionPhotoUri);

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
                        String caption = captionEditText.getText().toString().trim();
                        //UploadFile upload = new UploadFile(downloadUri.toString(), timeStamp, caption);
                        DocumentReference docRef = firestoreDB.collection("Photos").
                                document();
                        UploadFile upload = new UploadFile(downloadUri.toString(), timeStamp, caption, docRef.getId(), userID);
                        docRef.set(upload);
                        //loadGallery();
                    } else {
                        // Handle failures
                        Toast.makeText(PhotoCaptionActivity.this, "Failed to get photo downloadUrl.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
}
