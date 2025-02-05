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
        String query;

        // Verifica se l'ordine con l'ID esiste già usando il metodo exists
        if (exists(ordine.getId())) {
            // Query per aggiornare l'ordine esistente
            query = "UPDATE ordine SET prodotti = ?, quantita = ? WHERE id = ?";
        } else {
            // Query per inserire un nuovo ordine
            query = "INSERT INTO ordine (id, prodotti, quantita) VALUES (?, ?, ?)";
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int parameterIndex = 1;

            if (exists(ordine.getId())) { // Aggiornamento
                stmt.setString(parameterIndex++, String.join(",", ordine.getProdotti())); // Serializza i prodotti
                stmt.setString(parameterIndex++, ordine.getQuantita().toString()); // Serializza le quantità
                stmt.setInt(parameterIndex, ordine.getId()); // WHERE clausola, l'ID
            } else { // Inserimento
                stmt.setInt(parameterIndex++, ordine.getId()); // L'ID dell'ordine
                stmt.setString(parameterIndex++, String.join(",", ordine.getProdotti())); // Serializza i prodotti
                stmt.setString(parameterIndex, ordine.getQuantita().toString()); // Serializza le quantità
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Operazione fallita: nessuna riga modificata.");
            }
        } catch (SQLException e) {
            throw new DatabaseConfigurationException("Errore durante il salvataggio/aggiornamento dell'ordine", e);
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
        String query = "SELECT id, prodotti, quantita FROM ordine WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrdine(rs); // Mappa i risultati in un oggetto Ordine
                } else {
                    throw new IllegalArgumentException("Ordine con ID " + id + " non trovato"); // ID non trovato
                }
            }
        } catch (SQLException e) {
            throw new DatabaseConfigurationException("Impossibile recuperare l'ordine con ID " + id, e); // Gestione dell'errore
        }
    }



    private Ordine mapResultSetToOrdine(ResultSet rs) throws SQLException {
        // Estrai l'id dal ResultSet
        int id = rs.getInt("id");

        // Gestione robusta per i prodotti
        String prodottiString = rs.getString("prodotti");
        List<String> prodotti = new ArrayList<>();
        if (prodottiString != null && !prodottiString.trim().isEmpty()) {
            prodotti = List.of(prodottiString.split(","));
        }

        // Gestione robusta per le quantità
        String quantitaString = rs.getString("quantita");
        List<Integer> quantita = new ArrayList<>();

        if (quantitaString != null && !quantitaString.trim().isEmpty()) {
            // Rimuove parentesti quadre (se esistono)
            quantitaString = quantitaString.replace("[", "").replace("]", "");
            for (String q : quantitaString.split(",\\s*")) { // Dividi la stringa su virgole, con o senza spazi
                if (!q.trim().isEmpty()) { // Ignora valori vuoti
                    try {
                        quantita.add(Integer.parseInt(q.trim())); // Parsing di ciascun valore
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING, e, () -> "Valore non valido trovato nella quantità: " + q);
                    }
                }
            }
        }

        // Controllo finale di coerenza (facoltativo)
        if (prodotti.size() != quantita.size()) {
            LOGGER.log(Level.WARNING, () -> "Disallineamento tra prodotti e quantità per l'ordine con ID: " + id);
        }

        // Restituisce un nuovo oggetto Ordine
        return new Ordine(id, prodotti, quantita);
    }

}