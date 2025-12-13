package de.htwberlin.dbtech.object;

import java.sql.Date;

public class Fahrzeug {

    private long fzId;              // FZ_ID (PK)
    private int ssklId;            // SSKL_ID (FK)
    private int nutzerId;          // NUTZER_ID (FK)
    private String kennzeichen;    // KENNZEICHEN
    private String fin;            // FIN
    private int achsen;            // ACHSEN
    private double gewicht;        // GEWICHT
    private Date anmeldedatum;     // ANMELDEDATUM
    private Date abmeldedatum;     // ABMELDEDATUM (kann null sein)
    private String zulassungsland; // ZULASSUNGSLAND

    public long getFzId() {
        return fzId;
    }

    public void setFzId(long fzId) {
        this.fzId = fzId;
    }

    public int getSsklId() {
        return ssklId;
    }

    public void setSsklId(int ssklId) {
        this.ssklId = ssklId;
    }

    public int getNutzerId() {
        return nutzerId;
    }

    public void setNutzerId(int nutzerId) {
        this.nutzerId = nutzerId;
    }

    public String getKennzeichen() {
        return kennzeichen;
    }

    public void setKennzeichen(String kennzeichen) {
        this.kennzeichen = kennzeichen;
    }

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public int getAchsen() {
        return achsen;
    }

    public void setAchsen(int achsen) {
        this.achsen = achsen;
    }

    public double getGewicht() {
        return gewicht;
    }

    public void setGewicht(double gewicht) {
        this.gewicht = gewicht;
    }

    public Date getAnmeldedatum() {
        return anmeldedatum;
    }

    public void setAnmeldedatum(Date anmeldedatum) {
        this.anmeldedatum = anmeldedatum;
    }

    public Date getAbmeldedatum() {
        return abmeldedatum;
    }

    public void setAbmeldedatum(Date abmeldedatum) {
        this.abmeldedatum = abmeldedatum;
    }

    public String getZulassungsland() {
        return zulassungsland;
    }

    public void setZulassungsland(String zulassungsland) {
        this.zulassungsland = zulassungsland;
    }
}
