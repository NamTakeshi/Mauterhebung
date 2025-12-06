package de.htwberlin.dbtech.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;

/**
 * Implementierung des FahrzeugDao mit JDBC
 *
 */
public class FahrzeugDaoImpl implements FahrzeugDao {

    private static final Logger L = LoggerFactory.getLogger(FahrzeugDaoImpl.class);
    private Connection connection;

    public FahrzeugDaoImpl(Connection connection) {
        this.connection = connection;
    }

    private Connection getConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }

    @Override
    public boolean isVehicleRegistered(String kennzeichen){
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

    @Override
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

    @Override
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
}

