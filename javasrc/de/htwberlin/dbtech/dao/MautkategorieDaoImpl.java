package de.htwberlin.dbtech.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;

/**
 * Implementierung des MautkategorieDao mit JDBC
 *
 */
public class MautkategorieDaoImpl implements MautkategorieDao {

    private static final Logger L = LoggerFactory.getLogger(MautkategorieDaoImpl.class);
    private Connection connection;

    public MautkategorieDaoImpl(Connection connection) {
        this.connection = connection;
    }

    private Connection getConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }

    @Override
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
}

