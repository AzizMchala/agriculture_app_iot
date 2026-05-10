package com.example.agri_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private CircularProgressIndicator loginProgress;
    private TextView tvError;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loginProgress = findViewById(R.id.loginProgress);
        tvError = findViewById(R.id.tvError);

        // Animate card entry
        View cardLogin = findViewById(R.id.cardLogin);
        cardLogin.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));

        // Pre-fill email if saved
        SharedPreferences prefs = getSharedPreferences("maison_prefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("saved_email", "");
        if (!savedEmail.isEmpty()) {
            etEmail.setText(savedEmail);
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            etEmail.setError("Email requis");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Mot de passe requis");
            return;
        }

        // Show loading
        btnLogin.setVisibility(View.GONE);
        loginProgress.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save login state
                        SharedPreferences prefs = getSharedPreferences("maison_prefs", MODE_PRIVATE);
                        prefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("saved_email", email)
                                .apply();

                        Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        overridePendingTransition(R.anim.fade_in, 0);
                        finish();
                    } else {
                        btnLogin.setVisibility(View.VISIBLE);
                        loginProgress.setVisibility(View.GONE);
                        tvError.setVisibility(View.VISIBLE);
                        String errorMsg = task.getException() != null
                                ? task.getException().getLocalizedMessage()
                                : "Erreur d'authentification";
                        tvError.setText(errorMsg);
                    }
                });
    }
}
