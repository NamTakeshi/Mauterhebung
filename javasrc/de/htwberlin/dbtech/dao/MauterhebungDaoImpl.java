package de.htwberlin.dbtech.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;

/**
 * Implementierung des MauterhebungDao mit JDBC
 *
 */
public class MauterhebungDaoImpl implements MauterhebungDao {

    private static final Logger L = LoggerFactory.getLogger(MauterhebungDaoImpl.class);
    private Connection connection;

    public MauterhebungDaoImpl(Connection connection) {
        this.connection = connection;
    }

    private Connection getConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }

    @Override
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

    @Override
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
}

