package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.object.Mauterhebung;

public interface MauterhebungDao {

    /**
     * Ermittelt die nächste freie MAUT_ID.
     * Typischerweise wird der maximale vorhandene Wert um 1 erhöht.
     *
     * @return nächste freie Maut-ID
     */
    int getNextMautId();

    Mauterhebung create(Mauterhebung maut);

    Mauterhebung getById(int mautId);

    void update(Mauterhebung maut);

    void delete(int mautId);
}
