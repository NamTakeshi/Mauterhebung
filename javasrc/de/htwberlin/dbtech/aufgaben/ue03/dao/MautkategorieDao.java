package de.htwberlin.dbtech.aufgaben.ue03.dao;

/**
 * Die Schnittstelle für Mautkategorie-Datenbankzugriffe
 *
 */
public interface MautkategorieDao {

    /**
     * Findet die passende Mautkategorie für automatisches Verfahren
     * anhand von Kennzeichen und Achszahl
     *
     * @param kennzeichen das Kennzeichen des Fahrzeugs
     * @param achszahl die Achszahl des Fahrzeugs
     * @return die KATEGORIE_ID
     */
    int findKategorieIdForAutomatic(String kennzeichen, int achszahl);

    /**
     * Holt den Mautsatz je Kilometer für eine Kategorie
     *
     * @param kategorieId die Kategorie-ID
     * @return der Mautsatz in Cent/km
     */
    double getMautsatzJeKm(int kategorieId);
}
