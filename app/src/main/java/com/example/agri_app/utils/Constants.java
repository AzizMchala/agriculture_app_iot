package com.example.agri_app.utils;

public class Constants {
    // Firebase paths
    public static final String PATH_CAPTEURS = "/maison/capteurs";
    public static final String PATH_ACTIONNEURS = "/maison/actionneurs";
    public static final String PATH_SERRURE = "/maison/serrure";

    // Sensor keys
    public static final String KEY_TEMPERATURE = "temperature";
    public static final String KEY_HUMIDITE = "humidite";
    public static final String KEY_PRESSION = "pression";
    public static final String KEY_LUMINOSITE = "luminosite";
    public static final String KEY_TIMESTAMP = "timestamp";

    // Actuator keys
    public static final String KEY_SERRURE = "serrure";
    public static final String KEY_LAMPE = "lampe";
    public static final String KEY_VENTILATEUR = "ventilateur";
    public static final String KEY_VANNE = "vanne";

    // Lock access keys
    public static final String KEY_DERNIER_ACCES = "dernier_acces";
    public static final String KEY_TENTATIVE_ECHOUEE = "tentative_echouee";

    // Automation thresholds
    public static final float TEMP_FAN_ON = 28.0f;
    public static final float TEMP_FAN_OFF = 20.0f;
    public static final float HUM_VALVE_ON = 50.0f;
    public static final float HUM_VALVE_OFF = 70.0f;
    public static final float LUX_LAMP_ON = 10000.0f;
    public static final float LUX_LAMP_OFF = 25000.0f;

    // SharedPreferences
    public static final String PREFS_NAME = "maison_prefs";
    public static final String PREF_EMAIL = "saved_email";
    public static final String PREF_LOGGED_IN = "is_logged_in";

    // Chart
    public static final int MAX_CHART_POINTS = 60;
}
