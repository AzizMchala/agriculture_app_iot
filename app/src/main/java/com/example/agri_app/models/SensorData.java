package com.example.agri_app.models;

public class SensorData {
    private float temperature;
    private float humidite;
    private float pression;
    private float luminosite;
    private long timestamp;

    public SensorData() {}

    public SensorData(float temperature, float humidite, float pression, float luminosite, long timestamp) {
        this.temperature = temperature;
        this.humidite = humidite;
        this.pression = pression;
        this.luminosite = luminosite;
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
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
