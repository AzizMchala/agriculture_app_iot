package com.example.agri_app.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.agri_app.R;
import com.example.agri_app.utils.Constants;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private LineChart lineChart;
    private TextView tvNoData, tvMinValue, tvMaxValue, tvAvgValue, tvDataPoints;
    private TabLayout tabSensors;

    private final List<Entry> tempEntries = new ArrayList<>();
    private final List<Entry> humEntries = new ArrayList<>();
    private final List<Entry> pressEntries = new ArrayList<>();
    private final List<Entry> lightEntries = new ArrayList<>();
    private int dataIndex = 0;
    private int selectedTab = 0;

    private int colorTemp, colorHum, colorPress, colorLight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.lineChart);
        tvNoData = view.findViewById(R.id.tvNoData);
        tvMinValue = view.findViewById(R.id.tvMinValue);
        tvMaxValue = view.findViewById(R.id.tvMaxValue);
        tvAvgValue = view.findViewById(R.id.tvAvgValue);
        tvDataPoints = view.findViewById(R.id.tvDataPoints);
        tabSensors = view.findViewById(R.id.tabSensors);

        colorTemp = ContextCompat.getColor(requireContext(), R.color.temp_start);
        colorHum = ContextCompat.getColor(requireContext(), R.color.humidity_start);
        colorPress = ContextCompat.getColor(requireContext(), R.color.pressure_start);
        colorLight = ContextCompat.getColor(requireContext(), R.color.light_start);

        setupTabs();
        setupChart();
        listenToData();
    }

    private void setupTabs() {
        tabSensors.addTab(tabSensors.newTab().setText(getString(R.string.sensor_temperature)));
        tabSensors.addTab(tabSensors.newTab().setText(getString(R.string.sensor_humidity)));
        tabSensors.addTab(tabSensors.newTab().setText(getString(R.string.sensor_pressure)));
        tabSensors.addTab(tabSensors.newTab().setText(getString(R.string.sensor_light)));

        tabSensors.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                updateChart();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupChart() {
        lineChart.setBackgroundColor(Color.TRANSPARENT);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setExtraBottomOffset(8f);

        // X Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#6B7280"));
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value) + "s";
            }
        });

        // Y Axis left
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#6B7280"));
        leftAxis.setTextSize(10f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#1A000000"));
        leftAxis.setDrawAxisLine(false);

        // Hide right axis
        lineChart.getAxisRight().setEnabled(false);

        // Legend
        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.parseColor("#1A1A2E"));
        legend.setTextSize(12f);

        lineChart.setNoDataText("");
        lineChart.invalidate();
    }

    private void listenToData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.PATH_CAPTEURS);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                Float temp = snapshot.child(Constants.KEY_TEMPERATURE).getValue(Float.class);
                Float hum = snapshot.child(Constants.KEY_HUMIDITE).getValue(Float.class);
                Float press = snapshot.child(Constants.KEY_PRESSION).getValue(Float.class);
                Float lux = snapshot.child(Constants.KEY_LUMINOSITE).getValue(Float.class);

                float x = dataIndex * 5f; // 5 seconds interval
                if (temp != null) tempEntries.add(new Entry(x, temp));
                if (hum != null) humEntries.add(new Entry(x, hum));
                if (press != null) pressEntries.add(new Entry(x, press));
                if (lux != null) lightEntries.add(new Entry(x, lux));

                // Trim to max points
                trimList(tempEntries);
                trimList(humEntries);
                trimList(pressEntries);
                trimList(lightEntries);

                dataIndex++;
                updateChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void trimList(List<Entry> list) {
        while (list.size() > Constants.MAX_CHART_POINTS) {
            list.remove(0);
        }
    }

    private void updateChart() {
        List<Entry> entries;
        String label;
        int color;
        String unit;

        switch (selectedTab) {
            case 1:
                entries = humEntries;
                label = getString(R.string.sensor_humidity);
                color = colorHum;
                unit = "%";
                break;
            case 2:
                entries = pressEntries;
                label = getString(R.string.sensor_pressure);
                color = colorPress;
                unit = " hPa";
                break;
            case 3:
                entries = lightEntries;
                label = getString(R.string.sensor_light);
                color = colorLight;
                unit = " lx";
                break;
            default:
                entries = tempEntries;
                label = getString(R.string.sensor_temperature);
                color = colorTemp;
                unit = "°C";
                break;
        }

        if (entries.isEmpty()) {
            lineChart.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
            tvMinValue.setText("--");
            tvMaxValue.setText("--");
            tvAvgValue.setText("--");
            tvDataPoints.setText("");
            return;
        }

        lineChart.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(1.5f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(40);
        dataSet.setHighLightColor(Color.parseColor("#4A7C2B"));

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.animateX(500);
        lineChart.invalidate();

        // Update stats
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE, sum = 0;
        for (Entry e : entries) {
            if (e.getY() < min) min = e.getY();
            if (e.getY() > max) max = e.getY();
            sum += e.getY();
        }
        float avg = sum / entries.size();

        tvMinValue.setText(String.format(Locale.getDefault(), "%.1f%s", min, unit));
        tvMaxValue.setText(String.format(Locale.getDefault(), "%.1f%s", max, unit));
        tvAvgValue.setText(String.format(Locale.getDefault(), "%.1f%s", avg, unit));
        tvDataPoints.setText(entries.size() + " " + getString(R.string.analytics_points));
    }
}
