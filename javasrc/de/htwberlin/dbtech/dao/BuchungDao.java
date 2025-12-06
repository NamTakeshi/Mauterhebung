package de.htwberlin.dbtech.dao;

import de.htwberlin.dbtech.object.Buchung;

/**
 * Die Schnittstelle für Buchungs-Datenbankzugriffe
 *
 */
public interface BuchungDao {

    /**
     * Prüft, ob die Achszahl zur gebuchten Mautkategorie passt (manuelles Verfahren)
     *
     * @param mautAbschnitt der Mautabschnitt
     * @param kennzeichen das Kennzeichen des Fahrzeugs
     * @param achszahl die gemessene Achszahl
     * @return true wenn Achszahl passt, false sonst
     */
    boolean isCorrectAxleCountManual(int mautAbschnitt, String kennzeichen, int achszahl);

    /**
     * Findet eine offene Buchung für einen Abschnitt und Kennzeichen
     *
     * @param mautAbschnitt der Mautabschnitt
     * @param kennzeichen das Kennzeichen des Fahrzeugs
     * @return die gefundene Buchung oder null
     */
    Buchung findOpenBooking(int mautAbschnitt, String kennzeichen);

    /**
     * Schließt eine Buchung ab (setzt B_ID auf 3 und Befahrungsdatum)
     *
     * @param buchung die Buchung, die abgeschlossen werden soll
     */
    void closeBooking(Buchung buchung);
}

