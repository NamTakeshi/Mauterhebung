package de.htwberlin.dbtech.object;

import java.util.Date;

public class Mauterhebung {

    private int mautId;           // MAUT_ID (PK)
    private int abschnittsId;     // ABSCHNITTS_ID (FK)
    private long fzgId;            // FZG_ID (FK)
    private int kategorieId;      // KATEGORIE_ID (FK)
    private Date befahrungsdatum; // BEFAHRUNGSDATUM
    private double kosten;        // KOSTEN

    public int getMautId() {
        return mautId;
    }

    public void setMautId(int mautId) {
        this.mautId = mautId;
    }

    public int getAbschnittsId() {
        return abschnittsId;
    }

    public void setAbschnittsId(int abschnittsId) {
        this.abschnittsId = abschnittsId;
    }

    public long getFzgId() {
        return fzgId;
    }

    public void setFzgId(long fzgId) {
        this.fzgId = fzgId;
    }

    public int getKategorieId() {
        return kategorieId;
    }

    public void setKategorieId(int kategorieId) {
        this.kategorieId = kategorieId;
    }

    public Date getBefahrungsdatum() {
        return befahrungsdatum;
    }

    public void setBefahrungsdatum(Date befahrungsdatum) {
        this.befahrungsdatum = befahrungsdatum;
    }

    public double getKosten() {
        return kosten;
    }

    public void setKosten(double kosten) {
        this.kosten = kosten;
    }
}
