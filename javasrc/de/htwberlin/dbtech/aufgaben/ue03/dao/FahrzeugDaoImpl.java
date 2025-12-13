package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.object.Fahrzeug;
import java.sql.*;

public class FahrzeugDaoImpl implements FahrzeugDao {

    private final Connection connection;

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
    public Fahrzeug create(Fahrzeug f) {
        String sql = "INSERT INTO FAHRZEUG (FZ_ID, SSKL_ID, NUTZER_ID, KENNZEICHEN, FIN, ACHSEN, GEWICHT, ANMELDEDATUM, ABMELDEDATUM, ZULASSUNGSLAND) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, f.getFzId());
            ps.setInt(2, f.getSsklId());
            ps.setInt(3, f.getNutzerId());
            ps.setString(4, f.getKennzeichen());
            ps.setString(5, f.getFin());
            ps.setInt(6, f.getAchsen());
            ps.setDouble(7, f.getGewicht());
            ps.setDate(8, f.getAnmeldedatum());
            ps.setDate(9, f.getAbmeldedatum());
            ps.setString(10, f.getZulassungsland());
            ps.executeUpdate();
            return f;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Fahrzeug getById(long fzId) {
        String sql = "SELECT * FROM FAHRZEUG WHERE FZ_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, fzId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToFahrzeug(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Fahrzeug getByKennzeichen(String kennzeichen) {
        String sql = "SELECT * FROM FAHRZEUG WHERE KENNZEICHEN = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, kennzeichen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToFahrzeug(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Fahrzeug f) {
        String sql = "UPDATE FAHRZEUG SET SSKL_ID = ?, NUTZER_ID = ?, KENNZEICHEN = ?, FIN = ?, " +
                "ACHSEN = ?, GEWICHT = ?, ANMELDEDATUM = ?, ABMELDEDATUM = ?, ZULASSUNGSLAND = ? " +
                "WHERE FZ_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, f.getSsklId());
            ps.setInt(2, f.getNutzerId());
            ps.setString(3, f.getKennzeichen());
            ps.setString(4, f.getFin());
            ps.setInt(5, f.getAchsen());
            ps.setDouble(6, f.getGewicht());
            ps.setDate(7, f.getAnmeldedatum());
            ps.setDate(8, f.getAbmeldedatum());
            ps.setString(9, f.getZulassungsland());
            ps.setLong(10, f.getFzId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(long fzId) {
        String sql = "DELETE FROM FAHRZEUG WHERE FZ_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, fzId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wandelt eine Datenbankzeile (ResultSet) in ein Objekt um.
     * Wird intern von getById() und ähnlichen Leseoperationen aufgerufen.
     *
     * @param rs ResultSet mit den Spaltendaten der Tabelle
     * @return neues Objekt mit den Werten aus der Datenbankzeile
     * @throws SQLException wenn beim Lesen der Spaltenwerte ein Fehler auftritt
     */
    private Fahrzeug mapRowToFahrzeug(ResultSet rs) throws SQLException {
        Fahrzeug f = new Fahrzeug();
        f.setFzId(rs.getLong("FZ_ID"));
        f.setSsklId(rs.getInt("SSKL_ID"));
        f.setNutzerId(rs.getInt("NUTZER_ID"));
        f.setKennzeichen(rs.getString("KENNZEICHEN"));
        f.setFin(rs.getString("FIN"));
        f.setAchsen(rs.getInt("ACHSEN"));
        f.setGewicht(rs.getDouble("GEWICHT"));
        f.setAnmeldedatum(rs.getDate("ANMELDEDATUM"));
        f.setAbmeldedatum(rs.getDate("ABMELDEDATUM"));   // kann null sein
        f.setZulassungsland(rs.getString("ZULASSUNGSLAND"));
        return f;
    }
}


