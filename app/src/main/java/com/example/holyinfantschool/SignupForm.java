package com.example.holyinfantschool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SignupForm extends AppCompatActivity {

    private EditText emailSignup, passwordSignup, confirmPassword;
    private Button signupButton, googleSignupButton;
    private TextView goToSignUp;
    private ImageView togglePassword, toggleConfirmPassword, btnBack;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_form);

        emailSignup = findViewById(R.id.emailSignup);
        passwordSignup = findViewById(R.id.passwordSignup);
        confirmPassword = findViewById(R.id.confirmPassword);
        signupButton = findViewById(R.id.signupButton);
        googleSignupButton = findViewById(R.id.googleSignupButton);
        goToSignUp = findViewById(R.id.gotosign);
        togglePassword = findViewById(R.id.togglePassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);
        btnBack = findViewById(R.id.btnback);

        mAuth = FirebaseAuth.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Handle Signup Button Click
        signupButton.setOnClickListener(v -> {
            String email = emailSignup.getText().toString().trim();
            String password = passwordSignup.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPass)) {
                Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isLoggedIn", true);
                                editor.apply();

                                Toast.makeText(getApplicationContext(), "Signup Successful", Toast.LENGTH_SHORT).show();

                                // Navigate to SplashScreen after signing up
                                startActivity(new Intent(SignupForm.this, SplashScreen.class));
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Navigate to LoginForm when "Already have an account? Login" is clicked
        goToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignupForm.this, LoginForm.class));
            finish();
        });

        // Toggle Password Visibility
        togglePassword.setOnClickListener(v -> {
            togglePasswordVisibility(passwordSignup, togglePassword);
            isPasswordVisible = !isPasswordVisible;
        });

        // Toggle Confirm Password Visibility
        toggleConfirmPassword.setOnClickListener(v -> {
            togglePasswordVisibility(confirmPassword, toggleConfirmPassword);
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        });

        // Handle Google Signup Button Click (Placeholder for Firebase Authentication)
        googleSignupButton.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Google Signup Clicked (Implement Auth)", Toast.LENGTH_SHORT).show();
            // TODO: Implement Google Authentication
        });

        // Handle Back Button Click (Navigate to Homepage)
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(SignupForm.this, Homepage.class));
            finish();
        });
    }

    // Helper function to toggle password visibility
    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon) {
        if (passwordField.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
        } else {
            passwordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility_on);
        }
        passwordField.setSelection(passwordField.getText().length());
    }
}
