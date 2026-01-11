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
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

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

    private static final String TEMP_STUDENT_PASSWORD = "student123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {

            String role = getSharedPreferences("HIS_APP", MODE_PRIVATE)
                    .getString("user_role", "");

            if (role.equals("student")) {
                startActivity(new Intent(this, Categorypage.class));
                finish();
                return;
            }

            if (role.equals("faculty")) {
                startActivity(new Intent(this, TeacherSite.class));
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_homepage);
        initUI();
        setupUI();
    }

    @SuppressLint("MissingInflatedId")
    private void initUI() {

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

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
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

        forgotStudentPassword.setOnClickListener(v ->
                sendResetLink(studentEmail.getText().toString()));

        forgotFacultyPassword.setOnClickListener(v ->
                sendResetLink(facultyEmail.getText().toString()));

        studentLoginConfirm.setOnClickListener(v ->
                handleLogin(
                        studentEmail.getText().toString(),
                        studentPassword.getText().toString(),
                        true
                ));

        facultyLoginConfirm.setOnClickListener(v ->
                handleLogin(
                        facultyEmail.getText().toString(),
                        facultyPassword.getText().toString(),
                        false
                ));

        exitBtn.setOnClickListener(v -> {
            saveAuthHistory("logout");
            firebaseAuth.signOut();
            finishAffinity();
        });
    }

    private void sendResetLink(String email) {
        if (TextUtils.isEmpty(email)) return;

        firebaseAuth.sendPasswordResetEmail(email);
    }

    private void handleLogin(String email, String password, boolean isStudentLogin) {

        if (isStudentLogin && password.equals(TEMP_STUDENT_PASSWORD)) {

            getSharedPreferences("HIS_APP", MODE_PRIVATE)
                    .edit()
                    .putString("last_uid", "TEMP")
                    .putString("last_email", "TEMP")
                    .putString("user_role", "student")
                    .apply();

            saveAuthHistory("login", "TEMP", "TEMP", "student", "temp");

            startActivity(new Intent(this, Categorypage.class));
            finish();
            return;
        }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) return;

        showLoading(true);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String collection = isStudentLogin ? "students" : "faculty";

                    firestore.collection(collection)
                            .whereEqualTo("email", email)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(query -> {

                                showLoading(false);

                                if (query.isEmpty()) {
                                    firebaseAuth.signOut();
                                    return;
                                }

                                DocumentSnapshot doc = query.getDocuments().get(0);

                                boolean isArchived = Boolean.TRUE.equals(doc.getBoolean("isArchived"));
                                boolean isActive = doc.getBoolean("isActive") == null ||
                                        Boolean.TRUE.equals(doc.getBoolean("isActive"));

                                if (isArchived || !isActive) {
                                    firebaseAuth.signOut();
                                    return;
                                }

                                getSharedPreferences("HIS_APP", MODE_PRIVATE)
                                        .edit()
                                        .putString("last_uid", firebaseAuth.getCurrentUser().getUid())
                                        .putString("last_email", email)
                                        .putString("user_role",
                                                isStudentLogin ? "student" : "faculty")
                                        .apply();

                                saveAuthHistory(
                                        "login",
                                        firebaseAuth.getCurrentUser().getUid(),
                                        email,
                                        isStudentLogin ? "student" : "faculty",
                                        "firebase"
                                );

                                startActivity(new Intent(
                                        this,
                                        isStudentLogin ? Categorypage.class : TeacherSite.class
                                ));
                                finish();
                            });
                });
    }

    private void saveAuthHistory(
            String action,
            String uid,
            String email,
            String role,
            String loginType
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", action);
        data.put("uid", uid);
        data.put("email", email);
        data.put("role", role);
        data.put("loginType", loginType);
        data.put("device", "Android");
        data.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection("auth_history").add(data);
    }

    private void saveAuthHistory(String action) {

        String uid = getSharedPreferences("HIS_APP", MODE_PRIVATE)
                .getString("last_uid", null);

        String email = getSharedPreferences("HIS_APP", MODE_PRIVATE)
                .getString("last_email", null);

        if (uid == null) return;

        saveAuthHistory(
                action,
                uid,
                email,
                getSharedPreferences("HIS_APP", MODE_PRIVATE)
                        .getString("user_role", "unknown"),
                "firebase"
        );
    }

    private void showLoading(boolean show) {
        View loading = findViewById(R.id.loadingOverlay);
        loading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
}
    