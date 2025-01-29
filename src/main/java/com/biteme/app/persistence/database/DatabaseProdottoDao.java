package com.biteme.app.persistence.database;

import com.biteme.app.entity.Prodotto;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.ProdottoDao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseProdottoDao implements ProdottoDao {

    private static final Logger LOGGER = Logger.getLogger(DatabaseProdottoDao.class.getName());
    private final Connection connection;

    public DatabaseProdottoDao() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new DatabaseConfigurationException("Impossibile connettersi al database a causa di una configurazione errata", e);
        }
    }

    @Override
    public Optional<Prodotto> load(Integer id) {
        String query = "SELECT id, nome, quantita, prezzo, categoria, data_scadenza, disponibile FROM prodotti WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProdotto(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante il caricamento del prodotto con ID: " + id);
        }
        return Optional.empty();
    }

    @Override
    public void store(Prodotto entity) {
        String query = "INSERT INTO prodotti (nome, quantita, prezzo, categoria, data_scadenza, disponibile) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getNome());
            stmt.setInt(2, entity.getQuantita());
            stmt.setBigDecimal(3, entity.getPrezzo());
            stmt.setString(4, entity.getCategoria());
            if (entity.getDataScadenza() != null) {
                stmt.setDate(5, Date.valueOf(entity.getDataScadenza()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            stmt.setBoolean(6, entity.isDisponibile());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento del prodotto fallito, nessuna riga aggiunta.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Inserimento del prodotto fallito, nessun ID generato.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante il salvataggio del prodotto");
        }
    }

    @Override
    public void delete(Integer id) {
        String query = "DELETE FROM prodotti WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, () -> "Nessun prodotto trovato con ID: " + id);
            } else {
                LOGGER.log(Level.INFO, () -> "Prodotto con ID: " + id + " eliminato con successo");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante l'eliminazione del prodotto con ID: " + id);
        }
    }

    @Override
    public boolean exists(Integer id) {
        String query = "SELECT COUNT(*) FROM prodotti WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante la verifica dell'esistenza del prodotto con ID: " + id);
            return false;
        }
    }

    @Override
    public List<Prodotto> getByCategoria(String categoria) {
        String query = "SELECT id, nome, quantita, prezzo, categoria, data_scadenza, disponibile FROM prodotti WHERE categoria = ?";
        List<Prodotto> prodotti = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prodotti.add(mapResultSetToProdotto(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> String.format("Errore durante il recupero dei prodotti per la categoria: %s", categoria));
        }
        return prodotti;
    }

    @Override
    public List<Prodotto> getByDisponibilita(boolean disponibilita) {
        String query = "SELECT id, nome, quantita, prezzo, categoria, data_scadenza, disponibile FROM prodotti WHERE disponibile = ?";
        List<Prodotto> prodotti = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, disponibilita);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prodotti.add(mapResultSetToProdotto(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> String.format("Errore durante il recupero dei prodotti con disponibilità: %s", disponibilita));
        }
        return prodotti;
    }

    private Prodotto mapResultSetToProdotto(ResultSet rs) throws SQLException {
        return new Prodotto(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getInt("quantita"),
                rs.getBigDecimal("prezzo"),
                rs.getString("categoria"),
                rs.getDate("data_scadenza") != null ? rs.getDate("data_scadenza").toLocalDate() : null,
                rs.getBoolean("disponibile")
        );
    }
}