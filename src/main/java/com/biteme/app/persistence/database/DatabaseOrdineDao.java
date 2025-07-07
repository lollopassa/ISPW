package com.biteme.app.persistence.database;

import com.biteme.app.entities.Ordine;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.OrdineDao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseOrdineDao implements OrdineDao {

    @Override
    public int create(Ordine o) {
        final String upsertSql =
                "INSERT INTO ordine (id, prodotti, quantita, prezzi) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "  prodotti = VALUES(prodotti), " +
                        "  quantita = VALUES(quantita), " +
                        "  prezzi   = VALUES(prezzi)";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(upsertSql)) {

            int id = (o.getId() > 0) ? o.getId() : nextId(c);
            ps.setInt(1, id);

            ps.setString(2, String.join(",", o.getProdotti()));
            ps.setString(3, o.getQuantita().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
            ps.setString(4, o.getPrezzi().stream()
                    .map(BigDecimal::toPlainString)
                    .collect(Collectors.joining(",")));

            ps.executeUpdate();
            return id;

        } catch (SQLException ex) {
            throw new DatabaseConfigurationException(
                    "Errore salva/aggiorna ordine (id=" + o.getId() + "): " + ex.getMessage(),
                    ex
            );
        }
    }

    private int nextId(Connection c) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id),0)+1 FROM ordine")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    public Optional<Ordine> read(int id) {
        final String sql = "SELECT id, prodotti, quantita, prezzi FROM ordine WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseConfigurationException(
                    "Errore caricando ordine con id " + id + ": " + ex.getMessage(),
                    ex
            );
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM ordine WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabaseConfigurationException(
                    "Errore eliminando ordine con id " + id + ": " + ex.getMessage(),
                    ex
            );
        }
    }

    @Override
    public List<Ordine> getAll() {
        final String sql = "SELECT id, prodotti, quantita, prezzi FROM ordine";
        List<Ordine> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new DatabaseConfigurationException(
                    "Errore recuperando tutti gli ordini: " + ex.getMessage(),
                    ex
            );
        }
    }

    private Ordine map(ResultSet rs) throws SQLException {
        return new Ordine(
                rs.getInt("id"),
                parseList(rs.getString("prodotti")),
                parseIntList(rs.getString("quantita")),
                parseBigList(rs.getString("prezzi"))
        );
    }

    private List<String> parseList(String s) {
        if (s == null || s.isBlank()) return List.of();
        String[] parts = s.split(",");
        return Arrays.stream(parts).map(String::trim).toList();
    }

    private List<Integer> parseIntList(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .map(Integer::valueOf)
                .toList();
    }

    private List<BigDecimal> parseBigList(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .map(BigDecimal::new)
                .toList();
    }
}
