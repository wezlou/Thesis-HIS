package com.example.holyinfantschool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Homepage extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private ImageView volumeOnButton, volumeOffButton, teacherSettingButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Firebase init
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // UI components
        usernameInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        volumeOnButton = findViewById(R.id.volumeOn);
        volumeOffButton = findViewById(R.id.volumeOff);
        teacherSettingButton = findViewById(R.id.teachersetting);

        // Background music setup
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        if (!isMusicPlaying) {
            mediaPlayer.start();
            isMusicPlaying = true;
        }

        // Hide volume controls initially
        volumeOnButton.setVisibility(ImageView.INVISIBLE);
        volumeOffButton.setVisibility(ImageView.INVISIBLE);

        // Teacher settings button toggles sound buttons
        teacherSettingButton.setOnClickListener(v -> {
            if (volumeOnButton.getVisibility() == ImageView.VISIBLE) {
                volumeOnButton.setVisibility(ImageView.INVISIBLE);
                volumeOffButton.setVisibility(ImageView.INVISIBLE);
            } else {
                volumeOnButton.setVisibility(ImageView.VISIBLE);
                volumeOffButton.setVisibility(ImageView.VISIBLE);
            }
        });

        volumeOnButton.setOnClickListener(v -> {
            if (!isMusicPlaying) {
                mediaPlayer.start();
                isMusicPlaying = true;
            }
        });

        volumeOffButton.setOnClickListener(v -> {
            if (isMusicPlaying) {
                mediaPlayer.pause();
                isMusicPlaying = false;
            }
        });

        // 🔹 Login button logic
        loginButton.setOnClickListener(v -> {
            String email = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            checkIfStudentOrTeacher(user.getUid());
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // 🔹 Auto-login if user already authenticated
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            checkIfStudentOrTeacher(currentUser.getUid());
        }
    }

    // ✅ Check Firestore if user is student; otherwise assume teacher
    private void checkIfStudentOrTeacher(String uid) {
        DocumentReference studentRef = firestore.collection("students").document(uid);
        studentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 🔹 Student found → go to Categorypage
                Toast.makeText(this, "Welcome, Student!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Homepage.this, Categorypage.class));
            } else {
                // 🔹 Not in students → teacher account
                Toast.makeText(this, "Welcome, Teacher!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Homepage.this, TeacherSite.class));
            }
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error checking user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isMusicPlaying) {
            mediaPlayer.pause();
            isMusicPlaying = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
