package com.example.logindemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the view now
        setContentView(R.layout.activity_main);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private boolean validateEmail() {
        String email = inputEmail.getEditText().getText().toString().trim();

        if (email.isEmpty()) {
            inputEmail.setError("Field can't be empty");
            return false;
        } else {
            inputEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = inputPassword.getEditText().getText().toString().trim();

        if (password.isEmpty()) {
            inputPassword.setError("Field can't be empty");
            return false;
        } else {
            inputPassword.setError(null);
            return true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // if user logged in, go to sign-in screen
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, SignedInActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    public void loginButtonClicked(View view) {
        if (!validateEmail() | !validatePassword()) {
            return;
        }
        String email = inputEmail.getEditText().getText().toString();
        String password = inputPassword.getEditText().getText().toString();
        progressBar.setVisibility(View.VISIBLE);

        //authenticate user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_LONG).show();
                            Log.e("MyTag", task.getException().toString());
                        } else {
                            Intent intent = new Intent(MainActivity.this, SignedInActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
    public void signupButtonClicked(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
