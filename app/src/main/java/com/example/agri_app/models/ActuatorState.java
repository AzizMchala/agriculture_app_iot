package com.example.agri_app.models;

public class ActuatorState {
    private boolean serrure;
    private boolean lampe;
    private boolean ventilateur;
    private boolean vanne;

    public ActuatorState() {}

    public boolean isSerrure() { return serrure; }
    public void setSerrure(boolean serrure) { this.serrure = serrure; }
    public boolean isLampe() { return lampe; }
    public void setLampe(boolean lampe) { this.lampe = lampe; }
    public boolean isVentilateur() { return ventilateur; }
    public void setVentilateur(boolean ventilateur) { this.ventilateur = ventilateur; }
    public boolean isVanne() { return vanne; }
    public void setVanne(boolean vanne) { this.vanne = vanne; }
}
