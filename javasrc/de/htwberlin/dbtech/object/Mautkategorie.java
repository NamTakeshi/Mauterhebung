package de.htwberlin.dbtech.object;

public class Mautkategorie {

    private int kategorieId;       // KATEGORIE_ID
    private int ssklId;            // SSKL_ID
    private String katBezeichnung; // KAT_BEZEICHNUNG
    private String achszahl;       // ACHSZAHL (z.B. "= 4", ">= 5")
    private double mautsatzJeKm;   // MAUTSATZ_JE_KM (Cent/km)

    public int getKategorieId() {
        return kategorieId;
    }

    public void setKategorieId(int kategorieId) {
        this.kategorieId = kategorieId;
    }

    public int getSsklId() {
        return ssklId;
    }

    public void setSsklId(int ssklId) {
        this.ssklId = ssklId;
    }

    public String getKatBezeichnung() {
        return katBezeichnung;
    }

    public void setKatBezeichnung(String katBezeichnung) {
        this.katBezeichnung = katBezeichnung;
    }

    public String getAchszahl() {
        return achszahl;
    }

    public void setAchszahl(String achszahl) {
        this.achszahl = achszahl;
    }

    public double getMautsatzJeKm() {
        return mautsatzJeKm;
    }

    public void setMautsatzJeKm(double mautsatzJeKm) {
        this.mautsatzJeKm = mautsatzJeKm;
    }
}
