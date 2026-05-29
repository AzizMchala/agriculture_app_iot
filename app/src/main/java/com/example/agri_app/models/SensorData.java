package com.example.agri_app.models;

public class SensorData {
    private float temperature;
    private float humidite;
    private float pression;
    private float luminosite;
    private float humiditeSol;
    private long timestamp;

    public SensorData() {}

    public SensorData(float temperature, float humidite, float pression, float luminosite, float humiditeSol, long timestamp) {
        this.temperature = temperature;
        this.humidite = humidite;
        this.pression = pression;
        this.luminosite = luminosite;
        this.humiditeSol = humiditeSol;
        this.timestamp = timestamp;
    }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
    public float getHumidite() { return humidite; }
    public void setHumidite(float humidite) { this.humidite = humidite; }
    public float getPression() { return pression; }
    public void setPression(float pression) { this.pression = pression; }
    public float getLuminosite() { return luminosite; }
    public void setLuminosite(float luminosite) { this.luminosite = luminosite; }
    public float getHumiditeSol() { return humiditeSol; }
    public void setHumiditeSol(float humiditeSol) { this.humiditeSol = humiditeSol; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
