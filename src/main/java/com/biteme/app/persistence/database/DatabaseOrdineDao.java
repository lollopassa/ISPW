package com.biteme.app.persistence.database;

import com.biteme.app.entity.Ordine;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.OrdineDao;

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
            throw new DatabaseConfigurationException("Impossibile connettersi al database a causa di una configurazione errata", e);
        }
    }

    @Override
    public Optional<Ordine> load(Integer id) {
        String query = "SELECT id, prodotti, quantita FROM ordine WHERE id = ?";
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
        String query = "INSERT INTO ordine (prodotti, quantita) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Convertire la lista in una stringa serializzata
            stmt.setString(1, String.join(",", ordine.getProdotti()));
            stmt.setString(2, ordine.getQuantita().toString());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento dell'ordine fallito, nessuna riga aggiunta.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ordine.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Inserimento dell'ordine fallito, nessun ID generato.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante il salvataggio dell'ordine");
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

    private Ordine mapResultSetToOrdine(ResultSet rs) throws SQLException {
        // Ricostruisce i prodotti e le quantitÃ  dalle stringhe serializzate (consulta un eventuale specifico separatore)
        List<String> prodotti = List.of(rs.getString("prodotti").split(","));
        List<Integer> quantita = new ArrayList<>();
        for (String q : rs.getString("quantita").replace("[", "").replace("]", "").split(", ")) {
            quantita.add(Integer.parseInt(q));
        }

        return new Ordine(
                rs.getInt("id"),
                prodotti,
                quantita
        );
    }
}
