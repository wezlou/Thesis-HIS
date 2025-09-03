package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginForm extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton, googleloginButton;
    private TextView goToSignUp;
    private ImageView togglePassword, btnBack;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        auth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        googleloginButton = findViewById(R.id.googleloginButton);
        goToSignUp = findViewById(R.id.gotosign);
        togglePassword = findViewById(R.id.togglePassword);
        btnBack = findViewById(R.id.btnback);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginForm.this, TeacherSite.class));
            finish();
        }

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        goToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginForm.this, SignupForm.class));
            finish();
        });

        // Toggle Password Visibility
        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_visibility_on);
            }
            passwordInput.setSelection(passwordInput.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });

        // Google Sign-In Placeholder
        googleloginButton.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Google Sign-In Clicked (Implement Auth)", Toast.LENGTH_SHORT).show();
            // TODO: Implement Google Sign-In authentication
        });

        // ✅ Handle Back Button Click (Navigate to Homepage)
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(LoginForm.this, Homepage.class));
            finish();
        });
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();

                        // Show LoginSplash before navigating to TeacherSite
                        Intent splashIntent = new Intent(LoginForm.this, loginsplash.class);
                        startActivity(splashIntent);

                        // Delay transition to TeacherSite for splash effect
                        new Handler().postDelayed(() -> {
                            startActivity(new Intent(LoginForm.this, TeacherSite.class));
                            finish();
                        }, 2000);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
