package de.htwberlin.dbtech.aufgaben.ue03.dao;
import de.htwberlin.dbtech.object.Buchung;


public interface BuchungDao {

        void updateBuchung(Buchung buchung);

        Buchung findBuchung(int buchungId);

        Buchung createBuchung(Buchung buchung);
}


