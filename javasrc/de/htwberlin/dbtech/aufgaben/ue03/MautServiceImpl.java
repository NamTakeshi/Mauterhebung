package de.htwberlin.dbtech.aufgaben.ue03;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import de.htwberlin.dbtech.aufgaben.ue03.dao.*;
import de.htwberlin.dbtech.object.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.exceptions.AlreadyCruisedException;
import de.htwberlin.dbtech.exceptions.InvalidVehicleDataException;
import de.htwberlin.dbtech.exceptions.UnkownVehicleException;
import de.htwberlin.dbtech.aufgaben.ue03.dao.BuchungDaoImpl;
import de.htwberlin.dbtech.aufgaben.ue03.dao.BuchungDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.FahrzeugDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.FahrzeugDaoImpl;


/**
 * Die Klasse realisiert den Maut-Service.
 *
 */
public class MautServiceImpl implements IMautService {

    private static final Logger L = LoggerFactory.getLogger(MautServiceImpl.class);
    private Connection connection;
    private BuchungDao buchungDao;
    private FahrzeugDao fahrzeugDao;
    private MautkategorieDao mautkategorieDao;
    private MautabschnittDao mautabschnittDao;
    private MauterhebungDao mauterhebungDao;

    /**
     * Setzt die JDBC-Verbindung für den Service und initialisiert alle benötigten DAO-Objekte.
     * Muss vor der ersten Nutzung des Services aufgerufen werden.
     *
     * @param connection offene JDBC-Verbindung zur Maut-Datenbank
     */
    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
        // DAOs hier direkt erzeugen:
        this.buchungDao = new BuchungDaoImpl(connection);
        this.fahrzeugDao = new FahrzeugDaoImpl(connection);
        this.mautkategorieDao = new MautkategorieDaoImpl(connection);
        this.mautabschnittDao = new MautabschnittDaoImpl(connection);
        this.mauterhebungDao = new MauterhebungDaoImpl(connection);
    }

    /**
     * Liefert die aktuell gesetzte JDBC-Verbindung oder wirft eine Exception, wenn keine gesetzt ist.
     * Wird nur intern von SQL-Hilfsmethoden in diesem Service verwendet.
     *
     * @return aktuell gesetzte JDBC-Verbindung
     * @throws DataException wenn noch keine Verbindung gesetzt wurde
     */
    private Connection getConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }



    @Override
    public void berechneMaut(int mautAbschnitt, int achszahl, String kennzeichen)
            throws UnkownVehicleException, InvalidVehicleDataException, AlreadyCruisedException {

        // Prüfung 1: ob Fahrzeug registriert ist
        if (!isVehicleRegistered(kennzeichen)) {
            throw new UnkownVehicleException();
        }

        // Prüfung 2: prüft ob die Fahrzeugdaten korrekt sind
        Fahrzeug fahrzeug = fahrzeugDao.getByKennzeichen(kennzeichen); // DAO hilft das richtige Fahrzeug anhand Kennzeichen zu holen
        boolean autoCorrect = (fahrzeug != null && fahrzeug.getAchsen() == achszahl); // das (jetzt) richtige Fahrzeug kann Achsen prüfen
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
            handleManual(mautAbschnitt, kennzeichen);
        }
    }

    /**
     * Führt die Mauterhebung im automatischen Verfahren durch.
     * Ermittelt Abschnitt, Fahrzeug, aktives Fahrzeuggerät, passende Kategorie, Kosten und speichert die Mauterhebung.
     *
     * @param mautAbschnitt ID des befahrenen Mautabschnitts
     * @param achszahl      gemessene Achszahl des Fahrzeugs
     * @param kennzeichen   Kennzeichen des Fahrzeugs
     * @throws RuntimeException wenn Abschnitt, Fahrzeug, Fahrzeuggerät oder Kategorie nicht gefunden werden
     */
    public void handleAutomatic(int mautAbschnitt, int achszahl, String kennzeichen) {

        // 1. Objekt Abschnitt holen (für Abschnittslänge)
        Mautabschnitt abschnitt = mautabschnittDao.getById(mautAbschnitt);
        if (abschnitt == null) {
            throw new RuntimeException("Mautabschnitt nicht gefunden: " + mautAbschnitt);
        }

        // 2. Objekt Fahrzeug holen (für FZ_ID)
        Fahrzeug fahrzeug = fahrzeugDao.getByKennzeichen(kennzeichen);
        if (fahrzeug == null) {
            throw new RuntimeException("Fahrzeug nicht gefunden: " + kennzeichen);
        }

        // 2b. AKTIVES FAHRZEUGGERÄT → FZG_ID holen
        String sql = "SELECT FZG_ID " +
                "FROM FAHRZEUGGERAT " +
                "WHERE FZ_ID = ? AND STATUS = 'active'";

        long fzgId;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, fahrzeug.getFzId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Kein aktives Fahrzeuggerät für Fahrzeug " + fahrzeug.getFzId());
                }
                fzgId = rs.getLong("FZG_ID");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // 3. Passende Kategorie ermitteln
        int kategorieId = findKategorieIdForAutomatic(kennzeichen, achszahl);

        // 4. Kosten berechnen
        Mautkategorie kategorie = mautkategorieDao.getById(kategorieId);
        if (kategorie == null) {
            throw new RuntimeException("Mautkategorie nicht gefunden: " + kategorieId);
        }

        double laenge = abschnitt.getLaenge();              // Meter
        double satzJeKm = kategorie.getMautsatzJeKm();      // Cent/km
        double laengeKm = laenge / 1000.0;
        double satzJeKmEuro = satzJeKm / 100.0;
        double kosten = laengeKm * satzJeKmEuro;

        // 5. Mauterhebung-Objekt bauen
        int mautId = mauterhebungDao.getNextMautId(); // Nächste verfügbare Maut-Id nehmen
        Mauterhebung mauterhebung = new Mauterhebung();
        mauterhebung.setMautId(mautId);
        mauterhebung.setAbschnittsId(mautAbschnitt);
        mauterhebung.setFzgId(fzgId);
        mauterhebung.setKategorieId(kategorieId);
        mauterhebung.setBefahrungsdatum(new java.util.Date());  // aktuelles Datum
        mauterhebung.setKosten(kosten);

        // 6. Speichern über DAO
        mauterhebungDao.create(mauterhebung);
    }

    /**
     * Schließt eine offene manuelle Buchung für einen Abschnitt ab.
     * Setzt den Status der Buchung auf „abgeschlossen“, wenn eine passende offene Buchung existiert.
     *
     * @param mautAbschnitt ID des befahrenen Mautabschnitts
     * @param kennzeichen   Kennzeichen des Fahrzeugs
     * @throws AlreadyCruisedException wenn keine offene Buchung für Abschnitt und Kennzeichen gefunden wird
     */
    public void handleManual(int mautAbschnitt, String kennzeichen)
            throws AlreadyCruisedException {
        // 1. Buchung als Objekt holen
        Buchung buchung = findOpenBooking(mautAbschnitt, kennzeichen);
        if (buchung == null) { // Strecke nicht mehr als "offen" gebucht?
            throw new AlreadyCruisedException();
        }

        // 2. Buchung abschließen (B_ID = 3, Datum setzen)
        buchung.setB_id(3);  // Objekt Buchung Status auf "3/ abgeschlossen" aktualisieren
        buchungDao.updateBuchung(buchung); // geändertes Objekt per DAO in DB schreiben
    }


    /**
     * Prüft, ob ein Fahrzeug für die Mauterhebung zugelassen ist.
     * Ein Fahrzeug gilt als registriert, wenn entweder ein aktives Fahrzeuggerät oder eine offene Buchung existiert.
     *
     * @param kennzeichen Kennzeichen des Fahrzeugs
     * @return true, wenn das Fahrzeug registriert ist, sonst false
     */
    private boolean isVehicleRegistered(String kennzeichen){
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
     * Prüft im manuellen Verfahren, ob die angegebene Achszahl zur gebuchten Mautkategorie passt.
     * Liest dazu die Achszahl-Bedingung aus der Buchung und wertet Operatoren wie „=“ oder „>=“ aus.
     *
     * @param mautAbschnitt ID des befahrenen Mautabschnitts
     * @param kennzeichen   Kennzeichen des Fahrzeugs
     * @param achszahl      gemessene Achszahl des Fahrzeugs
     * @return true, wenn die Achszahl zur gebuchten Kategorie passt, sonst false
     */
    private boolean isCorrectAxleCountManual(int mautAbschnitt, String kennzeichen, int achszahl) {
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
     * Prüft, ob für das angegebene Kennzeichen ein aktives Fahrzeuggerät vorhanden ist.
     * Dient zur Entscheidung, ob das automatische Mautverfahren verwendet werden kann.
     *
     * @param kennzeichen Kennzeichen des Fahrzeugs
     * @return true, wenn ein aktives Fahrzeuggerät existiert, sonst false
     */
    private boolean hasActiveDevice(String kennzeichen) {
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

    /**
     * Ermittelt im automatischen Verfahren die passende Mautkategorie für Kennzeichen und Achszahl (für Kostenberechnung).
     * Verwendet die Schadstoffklasse des Fahrzeugs und die in der Datenbank hinterlegten Achszahlregeln.
     *
     * @param kennzeichen Kennzeichen des Fahrzeugs
     * @param achszahl    gemessene Achszahl des Fahrzeugs
     * @return ID der passenden Mautkategorie
     * @throws RuntimeException wenn keine passende Mautkategorie gefunden wird oder ein SQL-Fehler auftritt
     */
    private int findKategorieIdForAutomatic(String kennzeichen, int achszahl) {
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

    /**
     * Sucht eine offene Buchung (Status B_ID = 1) zu Abschnitt und Kennzeichen in der Datenbank.
     * Wird im manuellen Verfahren verwendet, um die passende Buchung für den Abschluss zu finden.
     *
     * @param mautAbschnitt ID des befahrenen Mautabschnitts
     * @param kennzeichen   Kennzeichen des Fahrzeugs
     * @return gefundene offene Buchung oder null, wenn keine offene Buchung existiert
     */
    private Buchung findOpenBooking(int mautAbschnitt, String kennzeichen) {
        String sql = "SELECT BUCHUNG_ID FROM BUCHUNG " +
                "WHERE ABSCHNITTS_ID = ? " +
                "AND KENNZEICHEN = ? " +
                "AND B_ID = 1"; // 1: offen

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, mautAbschnitt);
            ps.setString(2, kennzeichen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int buchungId = rs.getInt("BUCHUNG_ID");
                    return buchungDao.findBuchung(buchungId); // DAO sucht nach diesem Buchungsobjekt
                } else {
                    return null; // Keine offene Buchung gefunden
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}




