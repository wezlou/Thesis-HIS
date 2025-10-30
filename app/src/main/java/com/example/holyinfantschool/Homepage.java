package com.example.holyinfantschool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Homepage extends AppCompatActivity {

    private Button studentLoginBtn, facultyLoginBtn, exitBtn;
    private ImageView volumeOn, volumeOff, teacherSetting;
    private LinearLayout studentOverlay, facultyOverlay;
    private EditText studentEmail, facultyEmail;
    private EditText studentPassword, facultyPassword;
    private TextView forgotStudentPassword, forgotFacultyPassword;
    private Button studentLoginConfirm, facultyLoginConfirm, closeStudentOverlay, closeFacultyOverlay;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Views
        studentLoginBtn = findViewById(R.id.student_login);
        facultyLoginBtn = findViewById(R.id.faculty_login);
        exitBtn = findViewById(R.id.exit);
        volumeOn = findViewById(R.id.volumeOn);
        volumeOff = findViewById(R.id.volumeOff);
        teacherSetting = findViewById(R.id.teachersetting);

        studentOverlay = findViewById(R.id.studentLoginOverlay);
        facultyOverlay = findViewById(R.id.facultyLoginOverlay);

        studentEmail = findViewById(R.id.studentEmail);
        studentPassword = findViewById(R.id.studentPassword);
        facultyEmail = findViewById(R.id.facultyEmail);
        facultyPassword = findViewById(R.id.facultyPassword);

        forgotStudentPassword = findViewById(R.id.forgotStudentPassword);
        forgotFacultyPassword = findViewById(R.id.forgotFacultyPassword);

        studentLoginConfirm = findViewById(R.id.studentLoginButton);
        facultyLoginConfirm = findViewById(R.id.facultyLoginButton);
        closeStudentOverlay = findViewById(R.id.closeStudentOverlay);
        closeFacultyOverlay = findViewById(R.id.closeFacultyOverlay);

        // Background Music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        isMusicPlaying = true;

        // Volume toggle
        teacherSetting.setOnClickListener(v -> {
            int visibility = volumeOn.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
            volumeOn.setVisibility(visibility);
            volumeOff.setVisibility(visibility);
        });

        volumeOn.setOnClickListener(v -> {
            if (!isMusicPlaying) {
                mediaPlayer.start();
                isMusicPlaying = true;
            }
        });

        volumeOff.setOnClickListener(v -> {
            if (isMusicPlaying) {
                mediaPlayer.pause();
                isMusicPlaying = false;
            }
        });

        // Show overlays
        studentLoginBtn.setOnClickListener(v -> studentOverlay.setVisibility(View.VISIBLE));
        facultyLoginBtn.setOnClickListener(v -> facultyOverlay.setVisibility(View.VISIBLE));
        closeStudentOverlay.setOnClickListener(v -> studentOverlay.setVisibility(View.GONE));
        closeFacultyOverlay.setOnClickListener(v -> facultyOverlay.setVisibility(View.GONE));

        // Forgot password
        forgotStudentPassword.setOnClickListener(v -> sendResetLink(studentEmail.getText().toString()));
        forgotFacultyPassword.setOnClickListener(v -> sendResetLink(facultyEmail.getText().toString()));

        // Login buttons
        studentLoginConfirm.setOnClickListener(v ->
                handleLogin(studentEmail.getText().toString(), studentPassword.getText().toString(), true));
        facultyLoginConfirm.setOnClickListener(v ->
                handleLogin(facultyEmail.getText().toString(), facultyPassword.getText().toString(), false));

        exitBtn.setOnClickListener(v -> finishAffinity());
    }

    private void sendResetLink(String email) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter email first.", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Reset link sent to email.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleLogin(String email, String password, boolean isStudent) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (isStudent) {
                        startActivity(new Intent(Homepage.this, Categorypage.class));
                        Toast.makeText(this, "Welcome Student!", Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(new Intent(Homepage.this, TeacherSite.class));
                        Toast.makeText(this, "Welcome Faculty!", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isMusicPlaying) mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isMusicPlaying) mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
}
