package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.object.Buchung;

/**
 * Implementierung des BuchungDao mit JDBC
 *
 */
public class BuchungDaoImpl implements BuchungDao {

    private static final Logger L = LoggerFactory.getLogger(BuchungDaoImpl.class);
    private Connection connection;

    public BuchungDaoImpl(Connection connection) {
        this.connection = connection;
    }

    private Connection getConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }

    @Override
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

    @Override
    public Buchung findOpenBooking(int mautAbschnitt, String kennzeichen) {
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

    @Override
    public void closeBooking(Buchung buchung) {
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
}

