package de.htwberlin.dbtech.aufgaben.ue03;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.htwberlin.dbtech.dao.*;
import de.htwberlin.dbtech.object.Buchung;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.exceptions.AlreadyCruisedException;
import de.htwberlin.dbtech.exceptions.InvalidVehicleDataException;
import de.htwberlin.dbtech.exceptions.UnkownVehicleException;

/**
 * Die Klasse realisiert den AusleiheService.
 * 
 * @author Patrick Dohmeier
 */
public class MautServiceImpl implements IMautService {

	private static final Logger L = LoggerFactory.getLogger(MautServiceImpl.class);
	private Connection connection;

    private FahrzeugDao fahrzeugDao;
    private BuchungDao buchungDao;
    private MautkategorieDao mautkategorieDao;
    private MautabschnittDao mautabschnittDao;
    private MauterhebungDao mauterhebungDao;

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	private Connection getConnection() {
		if (connection == null) {
			throw new DataException("Connection not set");
		}
		return connection;
	}

	@Override
	public void berechneMaut(int mautAbschnitt, int achszahl, String kennzeichen)
			throws UnkownVehicleException, InvalidVehicleDataException, AlreadyCruisedException {

        // Injiziere oder erstelle den DAO
        if (fahrzeugDao == null) {
            fahrzeugDao = new FahrzeugDaoImpl(getConnection());
        }

        // Prüfung 1: ob Fahrzeug registriert ist
        if (!fahrzeugDao.isVehicleRegistered(kennzeichen)) {
            throw new UnkownVehicleException();
        }

        // Prüfung 2: prüft ob die Fahrzeugdaten korrekt sind
        if (buchungDao == null) {
            buchungDao = new BuchungDaoImpl(getConnection());
        }
        boolean autoCorrect = fahrzeugDao.isCorrectAxleCountAutomatic(kennzeichen, achszahl);
        boolean manualCorrect = buchungDao.isCorrectAxleCountManual(mautAbschnitt, kennzeichen, achszahl);

        if (!autoCorrect && !manualCorrect) {
            // beide Pfade schlagen fehl → Exception
            throw new InvalidVehicleDataException();
        }

        // 3. Verfahren bestimmen
        if (fahrzeugDao.hasActiveDevice(kennzeichen)) {
            // automatisches Verfahren
            handleAutomatic(mautAbschnitt, achszahl, kennzeichen);
        } else {
            // manuelles Verfahren
            handleManual(mautAbschnitt, achszahl, kennzeichen);
        }
	}


    // Orchestrierung: handleAutomatic
    public void handleAutomatic(int mautAbschnitt, int achszahl, String kennzeichen) {
        if (mautkategorieDao == null) {
            mautkategorieDao = new MautkategorieDaoImpl(getConnection());
        }
        if (mautabschnittDao == null) {
            mautabschnittDao = new MautabschnittDaoImpl(getConnection());
        }
        if (mauterhebungDao == null) {
            mauterhebungDao = new MauterhebungDaoImpl(getConnection());
        }

        int kategorieId = mautkategorieDao.findKategorieIdForAutomatic(kennzeichen, achszahl);


        double laenge = mautabschnittDao.getAbschnittsLaenge(mautAbschnitt);  // in Metern
        double satzJeKm = mautkategorieDao.getMautsatzJeKm(kategorieId);      // in Cent/km

        // Länge von Metern in Kilometer
        double laengeKm = laenge / 1000.0;

        // Satz von Cent in Euro
        double satzJeKmEuro = satzJeKm / 100.0;

        // Kosten berechnen
        double kosten = laengeKm * satzJeKmEuro;

        // neue Mauterhebung anlegen
        int mautId = mauterhebungDao.getNextMautId();
        mauterhebungDao.insertMauterhebung(mautId, mautAbschnitt, kennzeichen, kategorieId, kosten);
    }


    // Orchestrierung: handleManual
    public void handleManual(int mautAbschnitt, int achszahl, String kennzeichen)
            throws AlreadyCruisedException {
        if (buchungDao == null) {
            buchungDao = new BuchungDaoImpl(getConnection());
        }
        // 1. Offene Buchung für diesen Abschnitt + Kennzeichen suchen
        Buchung buchung = buchungDao.findOpenBooking(mautAbschnitt, kennzeichen);
        if (buchung == null) {
            throw new AlreadyCruisedException();
        }

        // 2. Buchung abschließen (B_ID = 3, Datum setzen)
        buchung.setB_id(3);  // Status „abgeschlossen"
        buchungDao.closeBooking(buchung);
    }












}
