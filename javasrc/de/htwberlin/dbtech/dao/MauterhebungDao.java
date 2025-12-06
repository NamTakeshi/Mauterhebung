package de.htwberlin.dbtech.dao;

/**
 * Die Schnittstelle für Mauterhebung-Datenbankzugriffe
 *
 */
public interface MauterhebungDao {

    /**
     * Bestimmt die nächste verfügbare Maut-ID
     *
     * @return die nächste MAUT_ID
     */
    int getNextMautId();

    /**
     * Speichert eine neue Mauterhebung in der Datenbank
     *
     * @param mautId die Maut-ID
     * @param mautAbschnitt der Mautabschnitt
     * @param kennzeichen das Kennzeichen des Fahrzeugs
     * @param kategorieId die Kategorie-ID
     * @param kosten die berechneten Kosten
     */
    void insertMauterhebung(int mautId, int mautAbschnitt, String kennzeichen,
                            int kategorieId, double kosten);
}

