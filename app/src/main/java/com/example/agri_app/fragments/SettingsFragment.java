package com.example.agri_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class SettingsFragment extends Fragment {

    private TextView tvFirebaseStatus, tvLastAccess, tvAccessFailed;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFirebaseStatus = view.findViewById(R.id.tvFirebaseStatus);
        tvLastAccess = view.findViewById(R.id.tvLastAccess);
        tvAccessFailed = view.findViewById(R.id.tvAccessFailed);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);

        // Check Firebase connection
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Listen to lock access log
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            requireContext().getSharedPreferences("maison_prefs", 0)
                    .edit()
                    .putBoolean("is_logged_in", false)
                    .apply();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
