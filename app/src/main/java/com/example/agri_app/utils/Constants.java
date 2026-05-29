package com.example.agri_app.utils;

public class Constants {
    // ═══ Firebase Paths ═══
    public static final String PATH_CAPTEURS = "/maison/capteurs";
    public static final String PATH_ACTIONNEURS = "/maison/actionneurs";
    public static final String PATH_SERRURE = "/maison/serrure";
    public static final String PATH_SEUILS = "/maison/seuils";

    // ═══ Sensor Keys ═══
    public static final String KEY_TEMPERATURE = "temperature";
    public static final String KEY_HUMIDITE = "humidite";
    public static final String KEY_PRESSION = "pression";
    public static final String KEY_LUMINOSITE = "luminosite";
    public static final String KEY_HUMIDITE_SOL = "humiditeSol";
    public static final String KEY_TIMESTAMP = "timestamp";

    // ═══ Actuator Keys ═══
    public static final String KEY_SERRURE = "serrure";
    public static final String KEY_LAMPE = "lampe";
    public static final String KEY_VENTILATEUR = "ventilateur";
    public static final String KEY_VANNE = "vanne";

    // ═══ Threshold Keys (seuils — définis UNIQUEMENT depuis l'app) ═══
    public static final String KEY_SEUIL_HAUT = "seuilHaut";
    public static final String KEY_SEUIL_BAS = "seuilBas";
    public static final String SEUIL_TEMPERATURE = "temperature";
    public static final String SEUIL_LUMINOSITE = "luminosite";
    public static final String SEUIL_HUMIDITE_SOL = "humiditeSol";

    // Valeurs par défaut proposées à l'utilisateur (uniquement pour l'UI)
    public static final float DEFAULT_TEMP_HAUT = 28.0f;
    public static final float DEFAULT_TEMP_BAS = 20.0f;
    public static final float DEFAULT_LUX_BAS = 10000.0f;
    public static final float DEFAULT_LUX_HAUT = 25000.0f;
    public static final float DEFAULT_SOL_BAS = 30.0f;
    public static final float DEFAULT_SOL_HAUT = 60.0f;

    // ═══ Lock Access Keys ═══
    public static final String KEY_DERNIER_ACCES = "dernier_acces";
    public static final String KEY_TENTATIVE_ECHOUEE = "tentative_echouee";

    // ═══ SharedPreferences ═══
    public static final String PREFS_NAME = "maison_prefs";
    public static final String PREF_EMAIL = "saved_email";
    public static final String PREF_LOGGED_IN = "is_logged_in";

    // ═══ Chart ═══
    public static final int MAX_CHART_POINTS = 60;
}
