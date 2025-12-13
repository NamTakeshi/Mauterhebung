package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.object.Mauterhebung;
import java.sql.*;
import java.util.Date;

public class MauterhebungDaoImpl implements MauterhebungDao {

    private final Connection connection;

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
        String sql = "SELECT NVL(MAX(MAUT_ID),0) + 1 AS NEXT_ID FROM MAUTERHEBUNG";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("NEXT_ID");
            }
            throw new RuntimeException("Fehler beim Ermitteln der nächsten MAUT_ID");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mauterhebung create(Mauterhebung m) {
        String sql = "INSERT INTO MAUTERHEBUNG " +
                "(MAUT_ID, ABSCHNITTS_ID, FZG_ID, KATEGORIE_ID, BEFAHRUNGSDATUM, KOSTEN) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, m.getMautId());
            ps.setInt(2, m.getAbschnittsId());
            ps.setLong(3, m.getFzgId());
            ps.setInt(4, m.getKategorieId());

            // KORREKT: Ternärer Operator vollständig
            ps.setDate(5, m.getBefahrungsdatum() != null ?
                    new java.sql.Date(m.getBefahrungsdatum().getTime()) : null);

            ps.setDouble(6, m.getKosten());
            ps.executeUpdate();
            return m;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Mauterhebung getById(int mautId) {
        String sql = "SELECT MAUT_ID, ABSCHNITTS_ID, FZG_ID, KATEGORIE_ID, BEFAHRUNGSDATUM, KOSTEN " +
                "FROM MAUTERHEBUNG WHERE MAUT_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, mautId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMauterhebung(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Mauterhebung m) {
        String sql = "UPDATE MAUTERHEBUNG " +
                "SET ABSCHNITTS_ID = ?, FZG_ID = ?, KATEGORIE_ID = ?, BEFAHRUNGSDATUM = ?, KOSTEN = ? " +
                "WHERE MAUT_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, m.getAbschnittsId());
            ps.setLong(2, m.getFzgId());
            ps.setInt(3, m.getKategorieId());
            ps.setDate(4, m.getBefahrungsdatum() != null ?
                    new java.sql.Date(m.getBefahrungsdatum().getTime()) : null);
            ps.setDouble(5, m.getKosten());
            ps.setInt(6, m.getMautId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int mautId) {
        String sql = "DELETE FROM MAUTERHEBUNG WHERE MAUT_ID = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, mautId);
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
    private Mauterhebung mapRowToMauterhebung(ResultSet rs) throws SQLException {
        Mauterhebung m = new Mauterhebung();
        m.setMautId(rs.getInt("MAUT_ID"));
        m.setAbschnittsId(rs.getInt("ABSCHNITTS_ID"));
        m.setFzgId(rs.getLong("FZG_ID"));
        m.setKategorieId(rs.getInt("KATEGORIE_ID"));
        m.setBefahrungsdatum(rs.getDate("BEFAHRUNGSDATUM"));
        m.setKosten(rs.getDouble("KOSTEN"));
        return m;
    }
}
