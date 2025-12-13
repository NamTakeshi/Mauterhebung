package de.htwberlin.dbtech.aufgaben.ue03.dao;
import de.htwberlin.dbtech.object.Mautkategorie;

/**
 * Die Schnittstelle für Mautkategorie-Datenbankzugriffe
 *
 */

public interface MautkategorieDao {

    Mautkategorie create(Mautkategorie kategorie);

    Mautkategorie getById(int kategorieId);

    void update(Mautkategorie kategorie);

    void delete(int kategorieId);
}

