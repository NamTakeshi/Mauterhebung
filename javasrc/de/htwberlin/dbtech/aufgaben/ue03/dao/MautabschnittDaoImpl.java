package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.object.Mautabschnitt;
import java.sql.*;

public class MautabschnittDaoImpl implements MautabschnittDao {

    private final Connection connection;

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
    public Mautabschnitt create(Mautabschnitt a) {
        String sql = "INSERT INTO MAUTABSCHNITT (ABSCHNITTS_ID, LAENGE) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, a.getAbschnittsId());
            ps.setDouble(2, a.getLaenge());
            ps.executeUpdate();
            return a;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mautabschnitt getById(int abschnittsId) {
        String sql = "SELECT ABSCHNITTS_ID, LAENGE FROM MAUTABSCHNITT WHERE ABSCHNITTS_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, abschnittsId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Mautabschnitt a = new Mautabschnitt();
                    a.setAbschnittsId(rs.getInt("ABSCHNITTS_ID"));
                    a.setLaenge(rs.getDouble("LAENGE"));
                    return a;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Mautabschnitt a) {
        String sql = "UPDATE MAUTABSCHNITT SET LAENGE = ? WHERE ABSCHNITTS_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, a.getLaenge());
            ps.setInt(2, a.getAbschnittsId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int abschnittsId) {
        String sql = "DELETE FROM MAUTABSCHNITT WHERE ABSCHNITTS_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, abschnittsId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
