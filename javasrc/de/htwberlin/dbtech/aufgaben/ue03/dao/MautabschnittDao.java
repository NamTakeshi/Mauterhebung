package de.htwberlin.dbtech.aufgaben.ue03.dao;
import de.htwberlin.dbtech.object.Mautabschnitt;
/**
 * Die Schnittstelle für Mautabschnitt-Datenbankzugriffe
 *
 */



public interface MautabschnittDao {

    Mautabschnitt create(Mautabschnitt abschnitt);

    Mautabschnitt getById(int abschnittsId);

    void update(Mautabschnitt abschnitt);

    void delete(int abschnittsId);
}


