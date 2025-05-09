package com.biteme.app.persistence.database;

import com.biteme.app.entities.Ordine;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.OrdineDao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseOrdineDao implements OrdineDao {

    private static final Logger LOGGER = Logger.getLogger(DatabaseOrdineDao.class.getName());
    private final Connection connection;

    public DatabaseOrdineDao() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new DatabaseConfigurationException(
                    "Impossibile connettersi al database a causa di una configurazione errata", e
            );
        }
    }

    @Override
    public Optional<Ordine> load(Integer id) {
        String query = "SELECT id, prodotti, quantita, prezzi FROM ordine WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrdine(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante il caricamento dell'ordine con ID: " + id);
        }
        return Optional.empty();
    }

    @Override
    public void store(Ordine ordine) {
        String query;
        boolean exists = ordine.getId() > 0 && exists(ordine.getId());
        if (exists) {
            query = "UPDATE ordine SET prodotti = ?, quantita = ?, prezzi = ? WHERE id = ?";
        } else {
            query = "INSERT INTO ordine (id, prodotti, quantita, prezzi) VALUES (?, ?, ?, ?)";
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int i = 1;
            String prodottiStr = String.join(",", ordine.getProdotti());
            String quantitaStr = ordine.getQuantita().toString()
                    .replace("[", "").replace("]", "");
            String prezziStr = ordine.getPrezzi().stream()
                    .map(BigDecimal::toString)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            if (exists) {
                stmt.setString(i++, prodottiStr);
                stmt.setString(i++, quantitaStr);
                stmt.setString(i++, prezziStr);
                stmt.setInt(i, ordine.getId());
            } else {
                stmt.setInt(i++, ordine.getId());
                stmt.setString(i++, prodottiStr);
                stmt.setString(i++, quantitaStr);
                stmt.setString(i, prezziStr);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Operazione fallita: nessuna riga modificata.");
            }
        } catch (SQLException e) {
            throw new DatabaseConfigurationException(
                    "Errore durante il salvataggio/aggiornamento dell'ordine", e
            );
        }
    }

    @Override
    public void delete(Integer id) {
        String query = "DELETE FROM ordine WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, () -> "Nessun ordine trovato con ID: " + id);
            } else {
                LOGGER.log(Level.INFO, () -> "Ordine con ID: " + id + " eliminato con successo");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante l'eliminazione dell'ordine con ID: " + id);
        }
    }

    @Override
    public boolean exists(Integer id) {
        String query = "SELECT COUNT(*) FROM ordine WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante la verifica dell'esistenza dell'ordine con ID: " + id);
            return false;
        }
    }

    @Override
    public Ordine getById(Integer id) {
        return load(id).orElseThrow(
                () -> new IllegalArgumentException("Ordine con ID " + id + " non trovato")
        );
    }

    private Ordine mapResultSetToOrdine(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        List<String> prodotti = parseStringList(rs.getString("prodotti"));
        List<Integer> quantita = parseIntegerList(rs.getString("quantita"), "quantità");
        List<BigDecimal> prezzi = parseBigDecimalList(rs.getString("prezzi"), "prezzo");

        if ((prodotti.size() != quantita.size() || quantita.size() != prezzi.size())
                && LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.warning(String.format(
                    "Disallineamento tra prodotti, quantità e prezzi per l'ordine con ID: %d", id
            ));
        }
        return new Ordine(id, prodotti, quantita, prezzi);
    }

    private List<String> parseStringList(String input) {
        if (input == null || input.trim().isEmpty()) return List.of();
        return List.of(input.split(","));
    }

    private List<Integer> parseIntegerList(String input, String fieldName) {
        List<Integer> result = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) return result;
        for (String s : input.split(",\\s*")) {
            if (!s.isBlank()) {
                try {
                    result.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, e, () -> "Valore non valido trovato nella " + fieldName + ": " + s);
                }
            }
        }
        return result;
    }

    private List<BigDecimal> parseBigDecimalList(String input, String fieldName) {
        List<BigDecimal> result = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) return result;
        for (String s : input.split(",\\s*")) {
            if (!s.isBlank()) {
                try {
                    result.add(new BigDecimal(s.trim()));
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, e, () -> "Valore non valido trovato nel " + fieldName + ": " + s);
                }
            }
        }
        return result;
    }
}
