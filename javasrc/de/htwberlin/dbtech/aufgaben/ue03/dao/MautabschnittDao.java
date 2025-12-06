package de.htwberlin.dbtech.aufgaben.ue03.dao;

/**
 * Die Schnittstelle für Mautabschnitt-Datenbankzugriffe
 *
 */
public interface MautabschnittDao {

    /**
     * Holt die Länge eines Mautabschnitts
     *
     * @param mautAbschnitt die Abschnitts-ID
     * @return die Länge in Metern
     */
    double getAbschnittsLaenge(int mautAbschnitt);
}

