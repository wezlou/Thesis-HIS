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
import com.google.firebase.firestore.DocumentSnapshot;

public class Homepage extends AppCompatActivity {

    private Button studentLoginBtn, facultyLoginBtn, exitBtn;
    private ImageView volumeOn, volumeOff, teacherSetting;
    private LinearLayout studentOverlay, facultyOverlay;
    private EditText studentEmail, studentPassword, facultyEmail, facultyPassword;
    private TextView forgotStudentPassword, forgotFacultyPassword;
    private Button studentLoginConfirm, facultyLoginConfirm, closeStudentOverlay, closeFacultyOverlay;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // UI BINDING
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

        // BACKGROUND MUSIC
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        setupUI();
    }

    private void setupUI() {

        teacherSetting.setOnClickListener(v -> {
            boolean show = volumeOn.getVisibility() != View.VISIBLE;
            volumeOn.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            volumeOff.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
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

        studentLoginBtn.setOnClickListener(v -> studentOverlay.setVisibility(View.VISIBLE));
        facultyLoginBtn.setOnClickListener(v -> facultyOverlay.setVisibility(View.VISIBLE));

        closeStudentOverlay.setOnClickListener(v -> studentOverlay.setVisibility(View.GONE));
        closeFacultyOverlay.setOnClickListener(v -> facultyOverlay.setVisibility(View.GONE));

        forgotStudentPassword.setOnClickListener(v -> sendResetLink(studentEmail.getText().toString()));
        forgotFacultyPassword.setOnClickListener(v -> sendResetLink(facultyEmail.getText().toString()));

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

    private void handleLogin(String email, String password, boolean isStudentLogin) {

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String collection = isStudentLogin ? "students" : "faculty";

                    // 🔥 Query Firestore USING EMAIL (not UID)
                    firestore.collection(collection)
                            .whereEqualTo("email", email)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(query -> {

                                showLoading(false);

                                if (query.isEmpty()) {
                                    Toast.makeText(this,
                                            isStudentLogin ?
                                                    "This account is not registered as a student." :
                                                    "This account is not registered as a teacher.",
                                            Toast.LENGTH_SHORT).show();

                                    firebaseAuth.signOut();
                                    return;
                                }

                                // --- ACCOUNT VALIDATION ---
                                DocumentSnapshot doc = query.getDocuments().get(0);

                                boolean isArchived = doc.getBoolean("isArchived") != null &&
                                        doc.getBoolean("isArchived");

                                boolean isActive = doc.getBoolean("isActive") == null ||
                                        doc.getBoolean("isActive");

                                if (isArchived) {
                                    Toast.makeText(this,
                                            "Your account is archived.",
                                            Toast.LENGTH_LONG).show();

                                    firebaseAuth.signOut();
                                    return;
                                }

                                if (!isActive) {
                                    Toast.makeText(this,
                                            "Your account is not active.",
                                            Toast.LENGTH_LONG).show();

                                    firebaseAuth.signOut();
                                    return;
                                }

                                // 🔥 SAVE ROLE LOCALLY
                                getSharedPreferences("HIS_APP", MODE_PRIVATE)
                                        .edit()
                                        .putString("user_role", isStudentLogin ? "student" : "faculty")
                                        .apply();

                                // --- REDIRECT ---
                                if (isStudentLogin) {
                                    startActivity(new Intent(this, Categorypage.class));
                                    Toast.makeText(this, "Welcome Student!", Toast.LENGTH_SHORT).show();
                                } else {
                                    startActivity(new Intent(this, TeacherSite.class));
                                    Toast.makeText(this, "Welcome Faculty!", Toast.LENGTH_SHORT).show();
                                }

                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this,
                            "Login failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        View loading = findViewById(R.id.loadingOverlay);
        loading.setVisibility(show ? View.VISIBLE : View.GONE);

        studentLoginConfirm.setEnabled(!show);
        facultyLoginConfirm.setEnabled(!show);
        studentLoginBtn.setEnabled(!show);
        facultyLoginBtn.setEnabled(!show);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isMusicPlaying) mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMusicPlaying) mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
}
