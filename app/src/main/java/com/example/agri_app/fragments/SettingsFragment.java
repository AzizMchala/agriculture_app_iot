package com.example.agri_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.agri_app.LoginActivity;
import com.example.agri_app.R;
import com.example.agri_app.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private TextView tvFirebaseStatus, tvLastAccess, tvAccessFailed;

    // Threshold inputs
    private EditText etTempHigh, etTempLow;
    private EditText etLuxHigh, etLuxLow;
    private EditText etSoilHigh, etSoilLow;

    // Current values & badges
    private TextView tvTempCurrent, tvLuxCurrent, tvSoilCurrent;
    private TextView tvTempBadge, tvLuxBadge, tvSoilBadge;

    private DatabaseReference seuilsRef, capteursRef, actionneursRef;

    // Tracking for badge updates
    private float currentTemp = 0, currentLux = 0, currentSoil = 0;
    private float loadedTempHigh = 0, loadedTempLow = 0;
    private float loadedLuxHigh = 0, loadedLuxLow = 0;
    private float loadedSoilHigh = 0, loadedSoilLow = 0;
    private boolean seuilsExistent = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        tvFirebaseStatus = view.findViewById(R.id.tvFirebaseStatus);
        tvLastAccess = view.findViewById(R.id.tvLastAccess);
        tvAccessFailed = view.findViewById(R.id.tvAccessFailed);

        etTempHigh = view.findViewById(R.id.etTempHigh);
        etTempLow = view.findViewById(R.id.etTempLow);
        etLuxHigh = view.findViewById(R.id.etLuxHigh);
        etLuxLow = view.findViewById(R.id.etLuxLow);
        etSoilHigh = view.findViewById(R.id.etSoilHigh);
        etSoilLow = view.findViewById(R.id.etSoilLow);

        tvTempCurrent = view.findViewById(R.id.tvTempCurrent);
        tvLuxCurrent = view.findViewById(R.id.tvLuxCurrent);
        tvSoilCurrent = view.findViewById(R.id.tvSoilCurrent);

        tvTempBadge = view.findViewById(R.id.tvTempBadge);
        tvLuxBadge = view.findViewById(R.id.tvLuxBadge);
        tvSoilBadge = view.findViewById(R.id.tvSoilBadge);

        MaterialButton btnApply = view.findViewById(R.id.btnApplyThresholds);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);

        // Firebase refs
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        seuilsRef = db.getReference(Constants.PATH_SEUILS);
        capteursRef = db.getReference(Constants.PATH_CAPTEURS);
        actionneursRef = db.getReference(Constants.PATH_ACTIONNEURS);

        setupConnectionListener();
        setupLockLogListener();
        loadThresholdsFromFirebase();
        listenToSensorData();
        listenToActuatorStates();

        btnApply.setOnClickListener(v -> applyThresholds());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            requireContext().getSharedPreferences(Constants.PREFS_NAME, 0)
                    .edit().putBoolean(Constants.PREF_LOGGED_IN, false).apply();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // ═══ CONNECTION STATUS ═══
    private void setupConnectionListener() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected != null && connected) {
                    tvFirebaseStatus.setText(R.string.settings_connected);
                    tvFirebaseStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_success));
                    tvFirebaseStatus.setBackgroundResource(R.drawable.bg_status_on);
                } else {
                    tvFirebaseStatus.setText(R.string.settings_disconnected);
                    tvFirebaseStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_danger));
                    tvFirebaseStatus.setBackgroundResource(R.drawable.bg_status_off);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ═══ LOCK LOG ═══
    private void setupLockLogListener() {
        DatabaseReference serrureRef = FirebaseDatabase.getInstance().getReference(Constants.PATH_SERRURE);
        serrureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                String dernierAcces = snapshot.child(Constants.KEY_DERNIER_ACCES).getValue(String.class);
                Boolean echouee = snapshot.child(Constants.KEY_TENTATIVE_ECHOUEE).getValue(Boolean.class);
                if (dernierAcces != null) {
                    tvLastAccess.setText(getString(R.string.settings_last_access) + " " + dernierAcces);
                }
                if (echouee != null && echouee) {
                    tvAccessFailed.setVisibility(View.VISIBLE);
                } else {
                    tvAccessFailed.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ═══ LOAD THRESHOLDS FROM FIREBASE ═══
    // Si aucun seuil n'existe, pré-remplir avec les valeurs par défaut
    // (mais ne PAS écrire dans Firebase tant que l'utilisateur n'a pas cliqué "Appliquer")
    private void loadThresholdsFromFirebase() {
        seuilsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                // Vérifier si les seuils existent dans Firebase
                boolean hasData = snapshot.exists() && snapshot.getChildrenCount() > 0;

                if (hasData) {
                    seuilsExistent = true;

                    Float tempHigh = snapshot.child(Constants.SEUIL_TEMPERATURE)
                            .child(Constants.KEY_SEUIL_HAUT).getValue(Float.class);
                    Float tempLow = snapshot.child(Constants.SEUIL_TEMPERATURE)
                            .child(Constants.KEY_SEUIL_BAS).getValue(Float.class);
                    if (tempHigh != null) { loadedTempHigh = tempHigh; etTempHigh.setText(String.valueOf(tempHigh)); }
                    if (tempLow != null)  { loadedTempLow = tempLow; etTempLow.setText(String.valueOf(tempLow)); }

                    Float luxHigh = snapshot.child(Constants.SEUIL_LUMINOSITE)
                            .child(Constants.KEY_SEUIL_HAUT).getValue(Float.class);
                    Float luxLow = snapshot.child(Constants.SEUIL_LUMINOSITE)
                            .child(Constants.KEY_SEUIL_BAS).getValue(Float.class);
                    if (luxHigh != null) { loadedLuxHigh = luxHigh; etLuxHigh.setText(String.valueOf(luxHigh)); }
                    if (luxLow != null)  { loadedLuxLow = luxLow; etLuxLow.setText(String.valueOf(luxLow)); }

                    Float soilHigh = snapshot.child(Constants.SEUIL_HUMIDITE_SOL)
                            .child(Constants.KEY_SEUIL_HAUT).getValue(Float.class);
                    Float soilLow = snapshot.child(Constants.SEUIL_HUMIDITE_SOL)
                            .child(Constants.KEY_SEUIL_BAS).getValue(Float.class);
                    if (soilHigh != null) { loadedSoilHigh = soilHigh; etSoilHigh.setText(String.valueOf(soilHigh)); }
                    if (soilLow != null)  { loadedSoilLow = soilLow; etSoilLow.setText(String.valueOf(soilLow)); }

                } else {
                    // Premier lancement — pré-remplir les champs avec les défauts
                    // L'utilisateur doit cliquer "Appliquer" pour les envoyer à Firebase/ESP32
                    seuilsExistent = false;
                    etTempHigh.setText(String.valueOf(Constants.DEFAULT_TEMP_HAUT));
                    etTempLow.setText(String.valueOf(Constants.DEFAULT_TEMP_BAS));
                    etLuxHigh.setText(String.valueOf(Constants.DEFAULT_LUX_HAUT));
                    etLuxLow.setText(String.valueOf(Constants.DEFAULT_LUX_BAS));
                    etSoilHigh.setText(String.valueOf(Constants.DEFAULT_SOL_HAUT));
                    etSoilLow.setText(String.valueOf(Constants.DEFAULT_SOL_BAS));
                }

                updateAllBadges();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ═══ REAL-TIME SENSOR DATA ═══
    private void listenToSensorData() {
        capteursRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                Float temp = snapshot.child(Constants.KEY_TEMPERATURE).getValue(Float.class);
                Float lux = snapshot.child(Constants.KEY_LUMINOSITE).getValue(Float.class);
                Float soil = snapshot.child(Constants.KEY_HUMIDITE_SOL).getValue(Float.class);

                if (temp != null) {
                    currentTemp = temp;
                    tvTempCurrent.setText(String.format(Locale.getDefault(),
                            "Valeur actuelle : %.1f °C", temp));
                }
                if (lux != null) {
                    currentLux = lux;
                    tvLuxCurrent.setText(String.format(Locale.getDefault(),
                            "Valeur actuelle : %.0f lx", lux));
                }
                if (soil != null) {
                    currentSoil = soil;
                    tvSoilCurrent.setText(String.format(Locale.getDefault(),
                            "Valeur actuelle : %.1f %%", soil));
                }

                updateAllBadges();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ═══ ACTUATOR STATES FOR BADGES ═══
    private void listenToActuatorStates() {
        actionneursRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                Boolean fan = snapshot.child(Constants.KEY_VENTILATEUR).getValue(Boolean.class);
                Boolean lamp = snapshot.child(Constants.KEY_LAMPE).getValue(Boolean.class);
                Boolean valve = snapshot.child(Constants.KEY_VANNE).getValue(Boolean.class);

                updateBadge(tvTempBadge, fan != null && fan);
                updateBadge(tvLuxBadge, lamp != null && lamp);
                updateBadge(tvSoilBadge, valve != null && valve);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ═══ BADGE UPDATES ═══
    private void updateAllBadges() {
        if (!seuilsExistent) return;
        updateBadge(tvTempBadge, currentTemp > loadedTempHigh && loadedTempHigh > 0);
        updateBadge(tvLuxBadge, currentLux < loadedLuxLow && loadedLuxLow > 0);
        updateBadge(tvSoilBadge, currentSoil < loadedSoilLow && loadedSoilLow > 0);
    }

    private void updateBadge(TextView badge, boolean active) {
        if (badge == null) return;
        if (active) {
            badge.setText(R.string.threshold_actuator_on);
            badge.setBackgroundResource(R.drawable.bg_threshold_badge_on);
            badge.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_success));
        } else {
            badge.setText(R.string.threshold_actuator_off);
            badge.setBackgroundResource(R.drawable.bg_threshold_badge_off);
            badge.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary));
        }
    }

    // ═══ APPLY THRESHOLDS TO FIREBASE ═══
    // C'est ICI et UNIQUEMENT ICI que les seuils sont écrits dans Firebase
    // L'ESP32 les reçoit via son stream et active les automatismes
    private void applyThresholds() {
        try {
            float tempHigh = Float.parseFloat(etTempHigh.getText().toString().trim());
            float tempLow = Float.parseFloat(etTempLow.getText().toString().trim());
            float luxHigh = Float.parseFloat(etLuxHigh.getText().toString().trim());
            float luxLow = Float.parseFloat(etLuxLow.getText().toString().trim());
            float soilHigh = Float.parseFloat(etSoilHigh.getText().toString().trim());
            float soilLow = Float.parseFloat(etSoilLow.getText().toString().trim());

            // Validation
            if (tempHigh <= tempLow || luxHigh <= luxLow || soilHigh <= soilLow) {
                Toast.makeText(requireContext(),
                        getString(R.string.threshold_error_range), Toast.LENGTH_LONG).show();
                return;
            }

            // Écriture dans Firebase — l'ESP32 les reçoit instantanément via stream
            seuilsRef.child(Constants.SEUIL_TEMPERATURE).child(Constants.KEY_SEUIL_HAUT).setValue(tempHigh);
            seuilsRef.child(Constants.SEUIL_TEMPERATURE).child(Constants.KEY_SEUIL_BAS).setValue(tempLow);
            seuilsRef.child(Constants.SEUIL_LUMINOSITE).child(Constants.KEY_SEUIL_HAUT).setValue(luxHigh);
            seuilsRef.child(Constants.SEUIL_LUMINOSITE).child(Constants.KEY_SEUIL_BAS).setValue(luxLow);
            seuilsRef.child(Constants.SEUIL_HUMIDITE_SOL).child(Constants.KEY_SEUIL_HAUT).setValue(soilHigh);
            seuilsRef.child(Constants.SEUIL_HUMIDITE_SOL).child(Constants.KEY_SEUIL_BAS).setValue(soilLow);

            // Feedback
            Toast.makeText(requireContext(),
                    getString(R.string.threshold_applied), Toast.LENGTH_SHORT).show();

            // Animation bouton
            View btn = getView() != null ? getView().findViewById(R.id.btnApplyThresholds) : null;
            if (btn != null) {
                btn.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                        .withEndAction(() -> btn.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                        .start();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    getString(R.string.threshold_error_invalid), Toast.LENGTH_LONG).show();
        }
    }
}
