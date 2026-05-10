package com.example.agri_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.agri_app.R;
import com.example.agri_app.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvTempValue, tvHumValue, tvPressValue, tvLightValue;
    private TextView tvLockStatus, tvLampStatus, tvFanStatus, tvValveStatus;
    private TextView tvLastUpdate;
    private SwipeRefreshLayout swipeRefresh;
    private DatabaseReference capteursRef, actionneursRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        tvTempValue = view.findViewById(R.id.tvTempValue);
        tvHumValue = view.findViewById(R.id.tvHumValue);
        tvPressValue = view.findViewById(R.id.tvPressValue);
        tvLightValue = view.findViewById(R.id.tvLightValue);
        tvLockStatus = view.findViewById(R.id.tvLockStatus);
        tvLampStatus = view.findViewById(R.id.tvLampStatus);
        tvFanStatus = view.findViewById(R.id.tvFanStatus);
        tvValveStatus = view.findViewById(R.id.tvValveStatus);
        tvLastUpdate = view.findViewById(R.id.tvLastUpdate);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        // Firebase references
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        capteursRef = db.getReference(Constants.PATH_CAPTEURS);
        actionneursRef = db.getReference(Constants.PATH_ACTIONNEURS);

        // Swipe refresh styling
        swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.accent_primary));
        swipeRefresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(requireContext(), R.color.surface_card));
        swipeRefresh.setOnRefreshListener(() ->
                swipeRefresh.setRefreshing(false));

        // Animate cards entry
        view.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));

        // Start listening
        listenToSensors();
        listenToActuators();
    }

    private void listenToSensors() {
        capteursRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                Float temp = snapshot.child(Constants.KEY_TEMPERATURE).getValue(Float.class);
                Float hum = snapshot.child(Constants.KEY_HUMIDITE).getValue(Float.class);
                Float press = snapshot.child(Constants.KEY_PRESSION).getValue(Float.class);
                Float lux = snapshot.child(Constants.KEY_LUMINOSITE).getValue(Float.class);

                if (temp != null) {
                    tvTempValue.setText(String.format(Locale.getDefault(), "%.1f°", temp));
                    animateValue(tvTempValue);
                }
                if (hum != null) {
                    tvHumValue.setText(String.format(Locale.getDefault(), "%.1f%%", hum));
                    animateValue(tvHumValue);
                }
                if (press != null) {
                    tvPressValue.setText(String.format(Locale.getDefault(), "%.0f", press));
                    animateValue(tvPressValue);
                }
                if (lux != null) {
                    tvLightValue.setText(String.format(Locale.getDefault(), "%.0f", lux));
                    animateValue(tvLightValue);
                }

                // Update timestamp
                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                tvLastUpdate.setText(getString(R.string.dashboard_last_update) + " " + time);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenToActuators() {
        actionneursRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                updateActuatorStatus(tvLockStatus,
                        snapshot.child(Constants.KEY_SERRURE).getValue(Boolean.class));
                updateActuatorStatus(tvLampStatus,
                        snapshot.child(Constants.KEY_LAMPE).getValue(Boolean.class));
                updateActuatorStatus(tvFanStatus,
                        snapshot.child(Constants.KEY_VENTILATEUR).getValue(Boolean.class));
                updateActuatorStatus(tvValveStatus,
                        snapshot.child(Constants.KEY_VANNE).getValue(Boolean.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateActuatorStatus(TextView tv, Boolean state) {
        if (state != null && state) {
            tv.setText(R.string.state_on);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_success));
        } else {
            tv.setText(R.string.state_off);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_danger));
        }
    }

    private void animateValue(View view) {
        view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                .start();
    }
}
