package com.example.agri_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.agri_app.R;
import com.example.agri_app.utils.Constants;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ControlFragment extends Fragment {

    private MaterialSwitch switchLamp, switchFan, switchValve, switchLock;
    private TextView tvLampState, tvFanState, tvValveState, tvLockState;
    private DatabaseReference actionneursRef;
    private boolean isUpdatingFromFirebase = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchLamp = view.findViewById(R.id.switchLamp);
        switchFan = view.findViewById(R.id.switchFan);
        switchValve = view.findViewById(R.id.switchValve);
        switchLock = view.findViewById(R.id.switchLock);
        tvLampState = view.findViewById(R.id.tvLampState);
        tvFanState = view.findViewById(R.id.tvFanState);
        tvValveState = view.findViewById(R.id.tvValveState);
        tvLockState = view.findViewById(R.id.tvLockState);

        actionneursRef = FirebaseDatabase.getInstance().getReference(Constants.PATH_ACTIONNEURS);

        // Switch listeners
        switchLamp.setOnCheckedChangeListener((btn, checked) -> {
            if (!isUpdatingFromFirebase) {
                actionneursRef.child(Constants.KEY_LAMPE).setValue(checked);
            }
        });

        switchFan.setOnCheckedChangeListener((btn, checked) -> {
            if (!isUpdatingFromFirebase) {
                actionneursRef.child(Constants.KEY_VENTILATEUR).setValue(checked);
            }
        });

        switchValve.setOnCheckedChangeListener((btn, checked) -> {
            if (!isUpdatingFromFirebase) {
                actionneursRef.child(Constants.KEY_VANNE).setValue(checked);
            }
        });

        switchLock.setOnCheckedChangeListener((btn, checked) -> {
            if (isUpdatingFromFirebase) return;
            if (checked) {
                // Show confirmation dialog for lock
                new AlertDialog.Builder(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                        .setTitle(getString(R.string.actuator_lock))
                        .setMessage(getString(R.string.control_lock_warning))
                        .setPositiveButton(getString(R.string.control_confirm), (d, w) ->
                                actionneursRef.child(Constants.KEY_SERRURE).setValue(true))
                        .setNegativeButton(getString(R.string.control_cancel), (d, w) -> {
                            isUpdatingFromFirebase = true;
                            switchLock.setChecked(false);
                            isUpdatingFromFirebase = false;
                        })
                        .setOnCancelListener(d -> {
                            isUpdatingFromFirebase = true;
                            switchLock.setChecked(false);
                            isUpdatingFromFirebase = false;
                        })
                        .show();
            } else {
                actionneursRef.child(Constants.KEY_SERRURE).setValue(false);
            }
        });

        // Listen for real-time state changes from Firebase
        listenToActuators();
    }

    private void listenToActuators() {
        actionneursRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                isUpdatingFromFirebase = true;

                Boolean lampe = snapshot.child(Constants.KEY_LAMPE).getValue(Boolean.class);
                Boolean fan = snapshot.child(Constants.KEY_VENTILATEUR).getValue(Boolean.class);
                Boolean vanne = snapshot.child(Constants.KEY_VANNE).getValue(Boolean.class);
                Boolean serrure = snapshot.child(Constants.KEY_SERRURE).getValue(Boolean.class);

                if (lampe != null) {
                    switchLamp.setChecked(lampe);
                    updateStateText(tvLampState, lampe);
                }
                if (fan != null) {
                    switchFan.setChecked(fan);
                    updateStateText(tvFanState, fan);
                }
                if (vanne != null) {
                    switchValve.setChecked(vanne);
                    updateStateText(tvValveState, vanne);
                }
                if (serrure != null) {
                    switchLock.setChecked(serrure);
                    updateStateText(tvLockState, serrure);
                }

                isUpdatingFromFirebase = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateStateText(TextView tv, boolean state) {
        if (state) {
            tv.setText(R.string.state_on);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_success));
        } else {
            tv.setText(R.string.state_off);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_danger));
        }
    }
}
