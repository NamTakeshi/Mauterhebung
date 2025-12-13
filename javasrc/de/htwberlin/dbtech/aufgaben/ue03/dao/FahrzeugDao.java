package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.object.Fahrzeug;

public interface FahrzeugDao {

    Fahrzeug create(Fahrzeug fahrzeug);

    Fahrzeug getById(long fzId);

    Fahrzeug getByKennzeichen(String kennzeichen);

    void update(Fahrzeug fahrzeug);

    void delete(long fzId);
}

