package com.example.agri_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.ivLogo);
        View glowCircle = findViewById(R.id.glowCircle);
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        TextView tvPowered = findViewById(R.id.tvPowered);

        // Initially hide elements
        ivLogo.setAlpha(0f);
        glowCircle.setAlpha(0f);
        tvAppName.setAlpha(0f);
        tvSubtitle.setAlpha(0f);
        tvPowered.setAlpha(0f);

        // Animate logo scale up
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        ivLogo.startAnimation(scaleUp);
        ivLogo.animate().alpha(1f).setDuration(800).setStartDelay(200).start();

        // Animate glow circle pulse
        glowCircle.animate().alpha(0.3f).setDuration(1000).setStartDelay(300).start();
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        glowCircle.startAnimation(pulse);

        // Animate app name
        tvAppName.animate().alpha(1f).translationY(0).setDuration(600).setStartDelay(600).start();
        tvAppName.setTranslationY(30f);

        // Animate subtitle
        tvSubtitle.animate().alpha(1f).translationY(0).setDuration(600).setStartDelay(900).start();
        tvSubtitle.setTranslationY(20f);

        // Animate powered text
        tvPowered.animate().alpha(1f).setDuration(800).setStartDelay(1200).start();

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check if already logged in
            boolean loggedIn = getSharedPreferences("maison_prefs", MODE_PRIVATE)
                    .getBoolean("is_logged_in", false);

            Intent intent;
            if (loggedIn) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, 0);
            finish();
        }, SPLASH_DELAY);
    }
}
