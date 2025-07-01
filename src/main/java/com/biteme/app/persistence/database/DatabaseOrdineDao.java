package com.biteme.app.persistence.database;

import com.biteme.app.entities.Ordine;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.OrdineDao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseOrdineDao implements OrdineDao {

    private static final Logger LOGGER = Logger.getLogger(DatabaseOrdineDao.class.getName());

    /* ---------- CREATE / UPSERT ---------- */
    @Override
    public int create(Ordine o) {
        final String upsertSql =
                "INSERT INTO ordine (id, prodotti, quantita, prezzi) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO UPDATE SET " +
                        "prodotti = EXCLUDED.prodotti, " +
                        "quantita = EXCLUDED.quantita, " +
                        "prezzi   = EXCLUDED.prezzi";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(upsertSql)) {

            int id = (o.getId() > 0) ? o.getId() : nextId(c);
            ps.setInt   (1, id);
            ps.setString(2, String.join(",", o.getProdotti()));
            ps.setString(3, o.getQuantita().toString().replaceAll("[\\[\\] ]", ""));
            ps.setString(4,
                    (o.getPrezzi() == null || o.getPrezzi().isEmpty())
                            ? ""
                            : o.getPrezzi().stream()
                            .map(BigDecimal::toString)
                            .reduce((a, b) -> a + "," + b)
                            .orElse(""));

            ps.executeUpdate();
            return id;

        } catch (SQLException ex) {
            throw new DatabaseConfigurationException("Errore salva/aggiorna ordine", ex);
        }
    }

    /* ---------- sequenza auto-ID ---------- */
    private int nextId(Connection c) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id),0)+1 FROM ordine")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /* ---------- READ ---------- */
    @Override
    public Optional<Ordine> read(int id) {
        final String sql = "SELECT id, prodotti, quantita, prezzi FROM ordine WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex, () -> "Errore load ordine " + id);
            return Optional.empty();
        }
    }

    /* ---------- DELETE ---------- */
    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM ordine WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            if (ps.executeUpdate() == 0)
                LOGGER.warning(() -> "Nessun ordine con ID " + id);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex, () -> "Errore delete ordine " + id);
        }
    }

    /* ---------- LIST ALL ---------- */
    @Override
    public List<Ordine> getAll() {
        final String sql = "SELECT id, prodotti, quantita, prezzi FROM ordine";
        List<Ordine> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex, () -> "Errore lista ordini");
        }
        return list;
    }

    /* ---------- mapper helper ---------- */
    private Ordine map(ResultSet rs) throws SQLException {
        return new Ordine(
                rs.getInt("id"),
                parseList     (rs.getString("prodotti")),
                parseIntList  (rs.getString("quantita")),
                parseBigList  (rs.getString("prezzi"))
        );
    }

    /* ---------- parsing helper ---------- */

    private List<String> parseList(String s) {
        if (s == null || s.trim().isEmpty()) return Collections.emptyList();
        List<String> list = new ArrayList<>();
        for (String t : s.split(",")) list.add(t.trim());
        return list;
    }

    private List<Integer> parseIntList(String s) {
        if (s == null || s.trim().isEmpty()) return Collections.emptyList();

        List<Integer> list = new ArrayList<>();
        for (String token : s.split(",")) {
            try {
                list.add(Integer.parseInt(token.trim()));
            } catch (NumberFormatException ex) {
                LOGGER.warning("Valore quantità non intero ignorato: '" + token + "'");
            }
        }
        return list;
    }

    private List<BigDecimal> parseBigList(String s) {
        if (s == null || s.trim().isEmpty()) return Collections.emptyList();

        List<BigDecimal> list = new ArrayList<>();
        for (String token : s.split(",")) {
            try {
                list.add(new BigDecimal(token.trim()));
            } catch (NumberFormatException ex) {
                LOGGER.warning("Valore prezzo non numerico ignorato: '" + token + "'");
            }
        }
        return list;
    }
}
