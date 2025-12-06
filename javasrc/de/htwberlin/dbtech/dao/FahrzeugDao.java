package de.htwberlin.dbtech.dao;

/**
 * Die Schnittstelle für Fahrzeug-Datenbankzugriffe
 *
 */
public interface FahrzeugDao {

    /**
     * Prüft, ob das Fahrzeug registriert ist (aktiv oder mit offener Buchung)
     *
     * @param kennzeichen das Kennzeichen des Fahrzeugs
     * @return true wenn registriert, false sonst
     */
    boolean isVehicleRegistered(String kennzeichen);

    /**
     * Prüft, ob das Fahrzeug ein aktives Fahrzeuggerät hat (automatisches Verfahren)
     *
     * @param kennzeichen das Kennzeichen des Fahrzeugs
     * @return true wenn aktives Gerät vorhanden, false sonst
     */
    boolean hasActiveDevice(String kennzeichen);

    /**
     * Prüft, ob die gemessene Achszahl zur registrierten Achszahl passt
     *
     * @param kennzeichen das Kennzeichen des Fahrzeugs
     * @param achszahl die gemessene Achszahl
     * @return true wenn korrekt, false sonst
     */
    boolean isCorrectAxleCountAutomatic(String kennzeichen, int achszahl);


}
