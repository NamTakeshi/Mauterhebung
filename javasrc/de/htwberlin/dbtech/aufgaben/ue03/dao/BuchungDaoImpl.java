package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.object.Buchung;

public class BuchungDaoImpl implements BuchungDao {

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
    public void updateBuchung(Buchung buchung) {
        PreparedStatement ps = null;
        int i = 0;
        String query = "UPDATE BUCHUNG SET b_id = ?, abschnitts_id = ?, kennzeichen = ? WHERE buchung_id = ?";

        try {
            ps = getConnection().prepareStatement(query);
            ps.setInt(1, buchung.getB_id());
            ps.setInt(2, buchung.getAbschnitts_id());
            ps.setString(3, buchung.getKennzeichen());
            ps.setInt(4, buchung.getBuchung_id());
            i = ps.executeUpdate();
            System.out.println("Es wurde(n) " + i + " Datensätze aktualisiert");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Buchung findBuchung(int buchungId) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = "SELECT * FROM BUCHUNG WHERE BUCHUNG_ID = ?";
        Buchung b = null;

        try {
            ps = getConnection().prepareStatement(query);
            ps.setInt(1, buchungId);
            rs = ps.executeQuery();

            if (rs.next()) {
                b = new Buchung();
                b.setBuchung_id(rs.getInt("BUCHUNG_ID"));
                b.setB_id(rs.getInt("B_ID"));
                b.setAbschnitts_id(rs.getInt("ABSCHNITTS_ID"));
                b.setKategorie_id(rs.getInt("KATEGORIE_ID"));
                b.setKennzeichen(rs.getString("KENNZEICHEN"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return b;
    }

    @Override
    public Buchung createBuchung(Buchung buchung) {
        String sql = "INSERT INTO BUCHUNG " +
                "(BUCHUNG_ID, B_ID, ABSCHNITTS_ID, KATEGORIE_ID, KENNZEICHEN) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, buchung.getBuchung_id());
            ps.setInt(2, buchung.getB_id());
            ps.setInt(3, buchung.getAbschnitts_id());
            ps.setInt(4, buchung.getKategorie_id());
            ps.setString(5, buchung.getKennzeichen());

            ps.executeUpdate();
            return buchung;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
