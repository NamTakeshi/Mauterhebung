package de.htwberlin.dbtech.aufgaben.ue03;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
		// Prüfung 1: ob Kennzeichen bekannt (siehe Baum)
        if(!isVehicleregistered(kennzeichen)) {
            throw new UnkownVehicleException();
        }

        // Prüfung 2: prüft ob die Fahrzeugdaten korrekt sind
	}

    /**
     * prüft ob das Kennzeichen bekannt ist oder eine Buchung vorliegt
     *
     * @param kennzeichen
     * @return true = bekannt || false = unbekannt
     * */
    // Hilfsmethode
    public boolean isVehicleregistered(String kennzeichen){
        String query = "SELECT COUNT(*) AS anzahl FROM (Select f.Kennzeichen FROM Fahrzeug f Join Fahrzeuggerat f2 on f.FZ_ID = f2.FZ_ID WHERE f.Kennzeichen = ? AND f.Abmeldedatum is NULL AND f2.STATUS = 'active' UNION ALL Select b.Kennzeichen FROM Buchung b  WHERE b.Kennzeichen = ? AND b.B_ID = 1)";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            ps = getConnection().prepareStatement(query);
            ps.setString(1, kennzeichen);
            ps.setString(2, kennzeichen);
            rs = ps.executeQuery();

            if(rs.next()){
                return rs.getInt("ANZAHL") > 0;
            } else  {
                return false;
            }
        } catch(SQLException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e){
            throw new RuntimeException(e);
        }
    }

}
