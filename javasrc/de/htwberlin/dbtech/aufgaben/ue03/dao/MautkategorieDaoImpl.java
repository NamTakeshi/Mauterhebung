package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.object.Mautkategorie;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MautkategorieDaoImpl implements MautkategorieDao {

    private final Connection connection;

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
    public Mautkategorie create(Mautkategorie k) {
        String sql = "INSERT INTO MAUTKATEGORIE " +
                "(KATEGORIE_ID, SSKL_ID, KAT_BEZEICHNUNG, ACHSZAHL, MAUTSATZ_JE_KM) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, k.getKategorieId());
            ps.setInt(2, k.getSsklId());
            ps.setString(3, k.getKatBezeichnung());
            ps.setString(4, k.getAchszahl());
            ps.setDouble(5, k.getMautsatzJeKm());
            ps.executeUpdate();
            return k;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mautkategorie getById(int kategorieId) {
        String sql = "SELECT KATEGORIE_ID, SSKL_ID, KAT_BEZEICHNUNG, ACHSZAHL, MAUTSATZ_JE_KM " +
                "FROM MAUTKATEGORIE WHERE KATEGORIE_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, kategorieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMautkategorie(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Mautkategorie k) {
        String sql = "UPDATE MAUTKATEGORIE " +
                "SET SSKL_ID = ?, KAT_BEZEICHNUNG = ?, ACHSZAHL = ?, MAUTSATZ_JE_KM = ? " +
                "WHERE KATEGORIE_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, k.getSsklId());
            ps.setString(2, k.getKatBezeichnung());
            ps.setString(3, k.getAchszahl());
            ps.setDouble(4, k.getMautsatzJeKm());
            ps.setInt(5, k.getKategorieId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int kategorieId) {
        String sql = "DELETE FROM MAUTKATEGORIE WHERE KATEGORIE_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, kategorieId);
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
    private Mautkategorie mapRowToMautkategorie(ResultSet rs) throws SQLException {
        Mautkategorie k = new Mautkategorie();
        k.setKategorieId(rs.getInt("KATEGORIE_ID"));
        k.setSsklId(rs.getInt("SSKL_ID"));
        k.setKatBezeichnung(rs.getString("KAT_BEZEICHNUNG"));
        k.setAchszahl(rs.getString("ACHSZAHL"));
        k.setMautsatzJeKm(rs.getDouble("MAUTSATZ_JE_KM"));
        return k;
    }
}
