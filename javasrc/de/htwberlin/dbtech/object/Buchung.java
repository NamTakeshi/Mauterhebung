package de.htwberlin.dbtech.object;

import java.sql.Date;

public class Buchung {

    public int buchung_id;

    public int b_id;

    public int abschnitts_id;

    public int kategorie_id;

    public String kennzeichen;

    public Date buchungsdatum;

    public Date befahrungsdatum;

    public float kosten;

    public int getBuchung_id() {
        return buchung_id;
    }

    public void setBuchung_id(int buchung_id) {
        this.buchung_id = buchung_id;
    }

    public int getB_id() {
        return b_id;
    }

    public void setB_id(int b_id) {
        this.b_id = b_id;
    }

    public int getAbschnitts_id() {
        return abschnitts_id;
    }

    public void setAbschnitts_id(int abschnitts_id) {
        this.abschnitts_id = abschnitts_id;
    }

    public int getKategorie_id() {
        return kategorie_id;
    }

    public void setKategorie_id(int kategorie_id) {
        this.kategorie_id = kategorie_id;
    }

    public String getKennzeichen() {
        return kennzeichen;
    }

    public void setKennzeichen(String kennzeichen) {
        this.kennzeichen = kennzeichen;
    }

    public Date getBuchungsdatum() {
        return buchungsdatum;
    }

    public void setBuchungsdatum(Date buchungsdatum) {
        this.buchungsdatum = buchungsdatum;
    }

    public Date getBefahrungsdatum() {
        return befahrungsdatum;
    }

    public void setBefahrungsdatum(Date befahrungsdatum) {
        this.befahrungsdatum = befahrungsdatum;
    }

    public float getKosten() {
        return kosten;
    }

    public void setKosten(float kosten) {
        this.kosten = kosten;
    }





}

