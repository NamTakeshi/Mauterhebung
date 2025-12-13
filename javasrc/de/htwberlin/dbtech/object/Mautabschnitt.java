package de.htwberlin.dbtech.object;

public class Mautabschnitt {

    private int abschnittsId;      // ABSCHNITTS_ID
    private double laenge;         // LAENGE
    private String startKoordinate; // START_KOORDINATE
    private String zielKoordinate;  // ZIEL_KOORDINATE
    private String name;            // NAME
    private String abschnittstyp;   // ABSCHNITTSTYP

    public int getAbschnittsId() {
        return abschnittsId;
    }

    public void setAbschnittsId(int abschnittsId) {
        this.abschnittsId = abschnittsId;
    }

    public double getLaenge() {
        return laenge;
    }

    public void setLaenge(double laenge) {
        this.laenge = laenge;
    }

    public String getStartKoordinate() {
        return startKoordinate;
    }

    public void setStartKoordinate(String startKoordinate) {
        this.startKoordinate = startKoordinate;
    }

    public String getZielKoordinate() {
        return zielKoordinate;
    }

    public void setZielKoordinate(String zielKoordinate) {
        this.zielKoordinate = zielKoordinate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbschnittstyp() {
        return abschnittstyp;
    }

    public void setAbschnittstyp(String abschnittstyp) {
        this.abschnittstyp = abschnittstyp;
    }
}
