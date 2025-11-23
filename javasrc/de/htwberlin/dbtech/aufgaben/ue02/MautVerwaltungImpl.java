package de.htwberlin.dbtech.aufgaben.ue02;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;

/**
 * Die Klasse realisiert die Mautverwaltung.
 * 
 * @author Patrick Dohmeier
 */
public class MautVerwaltungImpl implements IMautVerwaltung {

	private static final Logger L = LoggerFactory.getLogger(MautVerwaltungImpl.class);
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

    /**
     * Liefert den Status eines Fahrzeugerätes zurück.
     *
     * @param fzg_id
     * - die ID des Fahrzeuggerätes
     * @return status - den Status des Fahrzeuggerätes
     **/
	@Override
	public String getStatusForOnBoardUnit(long fzg_id) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = "SELECT status FROM fahrzeuggerat f WHERE f.FZG_ID = ?"; // SQL Abfrage
        String result = "";

        try{
            ps = getConnection().prepareStatement(query); // DB Verbindung verwalten. //PreparedStatement = SQL-Anweisung mit Platzhaltern vorbereiten
            ps.setLong(1, fzg_id); // Parameter einsetzen
            rs = ps.executeQuery(); // Abfrage durchführen

            if(rs.next()){
                result = rs.getString("status"); // Ergebnisse in Java-Objekte umwandeln
            }
        }
        catch(SQLException e){
            throw new RuntimeException(e); // SQL-Fehler werden in Java-Exceptions übersetzt.
        }
        catch(NullPointerException e){
            throw new RuntimeException(e);
        }
		return result;
	}

    /**
     * Liefert die Nutzernummer für eine Mauterhebung, die durch ein Fahrzeug im
     * Automatischen Verfahren ausgelöst worden ist.
     *
     * @param maut_id
     * - die ID aus der Mauterhebung
     * @return nutzer_id - die Nutzernummer des Fahrzeughalters
     **/
	@Override
    public int getUsernumber(int maut_id) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        int result = 0;
        String query = "SELECT f.nutzer_id from mauterhebung m join fahrzeuggerat g on m.fzg_id = g.fzg_id  join fahrzeug f on g.fz_id = f.fz_id where m.maut_id = ?"; // ? wird ersetzt mit maut_id

        try{
            ps = getConnection().prepareStatement(query);
            ps.setInt(1, maut_id); // 1 ist 1. Platzhalter für 1. "?"für später maut_id
            rs = ps.executeQuery(); // Ergebnis wird in Tabelle ResultSet gespeichert

            if(rs.next()){ // rs.next() bewegt den Cursor zur ersten Zeile des Ergebnisses.
                result = rs.getInt("nutzer_id"); // falls true, wird aus Spalte der Ergebnistabelle gelesen und in result gespeichert
            }
        } catch(SQLException e) {
            throw new RuntimeException(e); // Wenn Datenbankfehler passiert, wird Exception ausgelöst, um das Programm zu kontrollieren
        } catch (NullPointerException e){
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Registriert ein Fahrzeug in der Datenbank für einen bestimmten Nutzer.
     *
     * @param fz_id
     * - die eindeutige ID des Fahrzeug
     * @param sskl_id
     * - die ID der Schadstoffklasse mit dem das Fahrzeug angemeldet
     * wird
     * @param nutzer_id
     * - der Nutzer auf dem das Fahrzeug angemeldet wird
     * @param kennzeichen
     * - das amtliche Kennzeichen des Fahrzeugs
     * @param fin
     * - die eindeutige Fahrzeugindentifikationsnummer
     * @param achsen
     * - die Anzahl der Achsen, die das Fahrzeug hat
     * @param gewicht
     * - das zulässige Gesamtgewicht des Fahrzeugs
     * @param zulassungsland
     * - die Landesbezeichnung für das Fahrzeug in dem es offiziell
     * angemeldet ist
     *
     * **/
    @Override
    public void registerVehicle(long fz_id, int sskl_id, int nutzer_id, String kennzeichen, String fin, int achsen,
                                int gewicht, String zulassungsland) {

        try (PreparedStatement ps = getConnection().prepareStatement("INSERT INTO FAHRZEUG " +
                "(FZ_ID, SSKL_ID, NUTZER_ID, KENNZEICHEN, FIN, ACHSEN, GEWICHT, ANMELDEDATUM, ZULASSUNGSLAND) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE, ?)")) {

            ps.setLong(1, fz_id);
            ps.setInt(2, sskl_id);
            ps.setInt(3, nutzer_id);
            ps.setString(4, kennzeichen);
            ps.setString(5, fin);
            ps.setInt(6, achsen);
            ps.setInt(7, gewicht);
            ps.setString(8, zulassungsland);
            ps.executeUpdate();

        } catch (SQLException e){
            throw new RuntimeException(e);
        } catch (NullPointerException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Aktualisiert den Status eines Fahrzeuggerätes in der Datenbank.
     *
     * @param fzg_id
     * - die ID des Fahrzeuggerätes
     * @param status
     * - der Status auf dem das Fahrzeuggerät aktualisiert werden
     * soll
     */
    @Override
    public void updateStatusForOnBoardUnit(long fzg_id, String status) {

        try (PreparedStatement ps = getConnection().prepareStatement("UPDATE FAHRZEUGGERAT SET STATUS = ? WHERE FZG_ID = ?")) {

            ps.setString(1, status);
            ps.setLong(2, fzg_id);
            ps.executeUpdate();

        } catch (SQLException e){
            throw new RuntimeException(e);
        } catch (NullPointerException e){
            throw new RuntimeException(e);
        }
    }

        /**
         * Löscht ein Fahrzeug in der Datenbank.
         *
         * @param fz_id
         * - die eindeutige ID des Fahrzeugs
         */
        @Override
        public void deleteVehicle(long fz_id) {

            try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM FAHRZEUG WHERE FZ_ID = ?")) {

                ps.setLong(1, fz_id);
                ps.executeUpdate();

            } catch (SQLException e){
                throw new RuntimeException(e);
            } catch (NullPointerException e){
                throw new RuntimeException(e);
            }
        }

    /**
     * liefert eine Liste von Mautabschnitten eines bestimmten Abschnittstypen
     * zurück. z.B. alle Mautabschnitte der Autobahn A10
     *
     * @param abschnittstyp
     * - der AbschnittsTyp kann bspw. eine bestimmte Autobahn (A10)
     * oder Bundesstrasse (B1) sein
     * @return List<Mautabschnitt> - eine Liste des Abschnittstypen, bspw. alle
     * Abschnitte der Autobahn A10
     **/
    @Override
    public List<Mautabschnitt> getTrackInformations(String abschnittstyp) {

        List<Mautabschnitt> result = new ArrayList<>();

        try (PreparedStatement ps = getConnection().prepareStatement("SELECT ABSCHNITTS_ID, LAENGE, START_KOORDINATE, ZIEL_KOORDINATE, NAME, ABSCHNITTSTYP " +
                "FROM MAUTABSCHNITT WHERE ABSCHNITTSTYP = ?")) {

            ps.setString(1, abschnittstyp);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Mautabschnitt m = new Mautabschnitt(
                            rs.getInt("ABSCHNITTS_ID"),
                            rs.getInt("LAENGE"),
                            rs.getString("START_KOORDINATE"),
                            rs.getString("ZIEL_KOORDINATE"),
                            rs.getString("NAME"),
                            rs.getString("ABSCHNITTSTYP")
                    );
                    result.add(m);
                }
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        } catch (NullPointerException e){
            throw new RuntimeException(e);
        }
        return result;
    }






}
