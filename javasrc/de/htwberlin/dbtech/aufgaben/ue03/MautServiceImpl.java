package de.htwberlin.dbtech.aufgaben.ue03;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        boolean autoCorrect = isCorrectAxleCountAutomatic(kennzeichen, achszahl);
        boolean manualCorrect = isCorrectAxleCountManual(mautAbschnitt, kennzeichen, achszahl);

        if (!autoCorrect && !manualCorrect) {
            // beide Pfade schlagen fehl → Exception
            throw new InvalidVehicleDataException();
        }

        // 3. Verfahren bestimmen
        if (hasActiveDevice(kennzeichen)) {
            // automatisches Verfahren
            handleAutomatic(mautAbschnitt, achszahl, kennzeichen);
        } else {
            // manuelles Verfahren
            handleManual(mautAbschnitt, achszahl, kennzeichen);
        }
	}


    // Hilfsmethoden, später in DAOs implementieren

    /**
     * prüft ob das Kennzeichen bekannt ist oder eine Buchung vorliegt
     *
     * @param kennzeichen
     * @return true = bekannt || false = unbekannt
     * */
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


    /**
     * Achszahl aus FAHRZEUG lesen (für automatisches Verfahren).
     * @param kennzeichen
     * @return
     */
    int getRegisteredAchszahl(String kennzeichen){

        return 1;
    }


    public boolean isCorrectAxleCountAutomatic(String kennzeichen, int achszahl){

        String sql = "SELECT f.ACHSEN " +
                "FROM FAHRZEUG f " +
                "WHERE f.KENNZEICHEN = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)){
            ps.setString(1, kennzeichen);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int registrierteAchsen = rs.getInt("ACHSEN");
                    return registrierteAchsen == achszahl;
                } else {
                    // sollte eigentlich nicht vorkommen, wenn vorher isVehicleRegistered(...) geprüft wurde
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean isCorrectAxleCountManual(int mautAbschnitt, String kennzeichen, int achszahl) {
        String sql = "SELECT k.ACHSZAHL " +
                "FROM BUCHUNG b " +
                "JOIN MAUTKATEGORIE k ON b.KATEGORIE_ID = k.KATEGORIE_ID " +
                "WHERE b.ABSCHNITTS_ID = ? " +
                "AND b.KENNZEICHEN = ? ";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, mautAbschnitt);
            ps.setString(2, kennzeichen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String achszahlStr = rs.getString("ACHSZAHL").trim();  // wegen "= 4" oder ">= 5"

                    // Extrahiere den numerischen Wert
                    if (achszahlStr.startsWith(">=")) {
                        int schwelle = Integer.parseInt(achszahlStr.substring(2).trim());
                        return achszahl >= schwelle;
                    } else if (achszahlStr.startsWith("=")) {
                        int wert = Integer.parseInt(achszahlStr.substring(1).trim());
                        return achszahl == wert;
                    } else {
                        // Fallback: versuche direkt zu parsen
                        int wert = Integer.parseInt(achszahlStr);
                        return achszahl == wert;
                    }
                } else {
                    return false; // keine passende offene Buchung
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prüft, ob ein aktives FAHRZEUGGERAET existiert → automatisches Verfahren.
     * @param kennzeichen
     * @return true = Fahrzeuggerät vorhanden || false = nicht vorhanden
     */
    public boolean hasActiveDevice(String kennzeichen) {
        String sql = "SELECT COUNT(*) AS ANZAHL " +
                "FROM FAHRZEUG f " +
                "JOIN FAHRZEUGGERAT g ON f.FZ_ID = g.FZ_ID " +
                "WHERE f.KENNZEICHEN = ? " +
                "AND g.STATUS = 'active'";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, kennzeichen);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ANZAHL") > 0;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Hilfsmethode für automatisches Verfahren: Abschnittslänge
    public double getAbschnittsLaenge(int mautAbschnitt) {
        String sql = "SELECT LAENGE FROM MAUTABSCHNITT WHERE ABSCHNITTS_ID = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, mautAbschnitt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("LAENGE");
                } else {
                    throw new RuntimeException("Mautabschnitt nicht gefunden: " + mautAbschnitt);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Hilfsmethode für automatisches Verfahren: Mautkategorie
    public int findKategorieIdForAutomatic(String kennzeichen, int achszahl) {
        String sql1 = "SELECT SSKL_ID FROM FAHRZEUG WHERE KENNZEICHEN = ?";
        String sql2 = "SELECT KATEGORIE_ID FROM MAUTKATEGORIE " +
                "WHERE SSKL_ID = ? " +
                "AND (ACHSZAHL = '= ' || ? OR ACHSZAHL LIKE '>= %') " + // weil sskl = 4 und achszahl >= 5 hat kategorie 16
                "ORDER BY KATEGORIE_ID DESC FETCH FIRST 1 ROW ONLY";

        try (PreparedStatement ps1 = getConnection().prepareStatement(sql1)) {
            ps1.setString(1, kennzeichen);
            try (ResultSet rs1 = ps1.executeQuery()) {
                rs1.next();
                int ssklId = rs1.getInt("SSKL_ID");

                try (PreparedStatement ps2 = getConnection().prepareStatement(sql2)) {
                    ps2.setInt(1, ssklId);
                    ps2.setInt(2, achszahl);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        if (rs2.next()) {
                            return rs2.getInt("KATEGORIE_ID");
                        } else {
                            throw new RuntimeException("Keine passende Mautkategorie gefunden");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    // Hilfsmethode: Mautsatz je km
    public double getMautsatzJeKm(int kategorieId) {
        String sql = "SELECT MAUTSATZ_JE_KM FROM MAUTKATEGORIE WHERE KATEGORIE_ID = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, kategorieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("MAUTSATZ_JE_KM");
                } else {
                    throw new RuntimeException("Mautkategorie nicht gefunden: " + kategorieId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Hilfsmethoden: neue MAUT_ID finden
    public int getNextMautId() {
        String sql = "SELECT COALESCE(MAX(MAUT_ID), 0) AS MAX_ID FROM MAUTERHEBUNG";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("MAX_ID") + 1;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Hilfsmethode: neuen Mauterhebung Eintrag erstellen
    public void insertMauterhebung(int mautId, int mautAbschnitt, String kennzeichen,
                                    int kategorieId, double kosten) {
        String sql =
                "INSERT INTO MAUTERHEBUNG (MAUT_ID, ABSCHNITTS_ID, FZG_ID, KATEGORIE_ID, BEFAHRUNGSDATUM, KOSTEN) " +
                        "SELECT ?, ?, g.FZG_ID, ?, CURRENT_DATE, ? " +  // <-- g.FZG_ID statt f.FZ_ID
                        "FROM FAHRZEUG f " +
                        "JOIN FAHRZEUGGERAT g ON f.FZ_ID = g.FZ_ID " +
                        "WHERE f.KENNZEICHEN = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, mautId);
            ps.setInt(2, mautAbschnitt);
            ps.setInt(3, kategorieId);
            ps.setDouble(4, kosten);
            ps.setString(5, kennzeichen);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Kein Fahrzeug für Kennzeichen gefunden: " + kennzeichen);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // Orchestrierung: handleAutomatic
    public void handleAutomatic(int mautAbschnitt, int achszahl, String kennzeichen) {
        int kategorieId = findKategorieIdForAutomatic(kennzeichen, achszahl);

        double laenge = getAbschnittsLaenge(mautAbschnitt);  // in Metern
        double satzJeKm = getMautsatzJeKm(kategorieId);      // in Cent/km

        // Länge von Metern in Kilometer
        double laengeKm = laenge / 1000.0;

        // Satz von Cent in Euro
        double satzJeKmEuro = satzJeKm / 100.0;

        // Kosten berechnen
        double kosten = laengeKm * satzJeKmEuro;

        // neue Mauterhebung anlegen
        int mautId = getNextMautId();
        insertMauterhebung(mautId, mautAbschnitt, kennzeichen, kategorieId, kosten);
    }


    // Hilfsmethoden für manuelles Verfahren
    private Buchung findOpenBooking(int mautAbschnitt, String kennzeichen) {
        String sql = "SELECT b.BUCHUNG_ID, b.B_ID, b.ABSCHNITTS_ID, b.KATEGORIE_ID, b.KENNZEICHEN " +
                "FROM BUCHUNG b " +
                "WHERE b.ABSCHNITTS_ID = ? " +
                "AND b.KENNZEICHEN = ? " +
                "AND b.B_ID = 1";  // Status 'offen'

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, mautAbschnitt);
            ps.setString(2, kennzeichen);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Buchung b = new Buchung();
                    b.setBuchung_id(rs.getInt("BUCHUNG_ID"));
                    b.setB_id(rs.getInt("B_ID"));
                    b.setAbschnitts_id(rs.getInt("ABSCHNITTS_ID"));
                    b.setKategorie_id(rs.getInt("KATEGORIE_ID"));
                    b.setKennzeichen(rs.getString("KENNZEICHEN"));
                    return b;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeBooking(Buchung buchung) {
        String sql = "UPDATE BUCHUNG SET B_ID = ?, BEFAHRUNGSDATUM = CURRENT_DATE WHERE BUCHUNG_ID = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, buchung.getB_id());
            ps.setInt(2, buchung.getBuchung_id());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Buchung konnte nicht aktualisiert werden: " + buchung.getBuchung_id());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleManual(int mautAbschnitt, int achszahl, String kennzeichen)
            throws AlreadyCruisedException {
        // 1. Offene Buchung für diesen Abschnitt + Kennzeichen suchen
        Buchung buchung = findOpenBooking(mautAbschnitt, kennzeichen);
        if (buchung == null) {
            // Keine offene Buchung → Doppelbefahrung
            throw new AlreadyCruisedException();
        }

        // 2. Buchung abschließen (B_ID = 3, Datum setzen)
        buchung.setB_id(3);  // Status „abgeschlossen"
        closeBooking(buchung);
    }












}
