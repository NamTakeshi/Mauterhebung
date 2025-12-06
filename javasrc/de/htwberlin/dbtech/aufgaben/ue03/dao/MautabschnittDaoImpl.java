package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.htwberlin.dbtech.exceptions.DataException;

/**
 * Implementierung des MautabschnittDao mit JDBC
 *
 */
public class MautabschnittDaoImpl implements MautabschnittDao {

    private static final Logger L = LoggerFactory.getLogger(MautabschnittDaoImpl.class);
    private Connection connection;

    public MautabschnittDaoImpl(Connection connection) {
        this.connection = connection;
    }

    private Connection getConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }

    @Override
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
}
