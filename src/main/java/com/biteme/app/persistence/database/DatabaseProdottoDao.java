package com.biteme.app.persistence.database;

import com.biteme.app.entity.Categoria;
import com.biteme.app.entity.Prodotto;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.ProdottoDao;

import java.sql.*;
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
        String query = "SELECT id, nome, prezzo, categoria, disponibile FROM prodotti WHERE id = ?";
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
        String query = "INSERT INTO prodotti (nome, prezzo, categoria, disponibile) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getNome());
            stmt.setBigDecimal(2, entity.getPrezzo());
            stmt.setString(3, entity.getCategoria().name());
            stmt.setBoolean(4, entity.isDisponibile());

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
        String query = "SELECT id, nome, prezzo, categoria, disponibile FROM prodotti WHERE categoria = ?";
        List<Prodotto> prodotti = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categoria.toUpperCase());
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
        String query = "SELECT id, nome, prezzo, categoria, disponibile FROM prodotti WHERE disponibile = ?";
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
                rs.getBigDecimal("prezzo"),
                Categoria.valueOf(rs.getString("categoria").toUpperCase()),
                rs.getBoolean("disponibile")
        );
    }

    @Override
    public List<Prodotto> getAll() {
        List<Prodotto> prodotti = new ArrayList<>();
        String query = "SELECT id, nome, prezzo, categoria, disponibile FROM prodotti";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Prodotto prodotto = mapResultSetToProdotto(rs);
                prodotti.add(prodotto);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Errore durante il recupero dei prodotti", ex);
        }

        return prodotti;
    }

    @Override
    public Prodotto findByNome(String nome) {
        // Elenca esplicitamente le colonne richieste
        String query = "SELECT id, nome, prezzo, categoria, disponibile FROM prodotti WHERE nome = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Crea un oggetto Prodotto dai valori del ResultSet
                    return new Prodotto(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getBigDecimal("prezzo"),
                            Categoria.valueOf(rs.getString("categoria")),
                            rs.getBoolean("disponibile")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore ricerca prodotto per nome", e);
        }
        return null;
    }

    @Override
    public void update(Prodotto prodotto) {
        String query = "UPDATE prodotti SET nome = ?, prezzo = ?, categoria = ?, disponibile = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, prodotto.getNome());
            stmt.setBigDecimal(2, prodotto.getPrezzo());
            stmt.setString(3, prodotto.getCategoria().name());
            stmt.setBoolean(4, prodotto.isDisponibile());
            stmt.setInt(5, prodotto.getId());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                LOGGER.log(Level.WARNING, () -> String.format("Nessun prodotto aggiornato con ID: %d", prodotto.getId()));
            } else {
                LOGGER.log(Level.INFO, () -> String.format("Prodotto aggiornato con successo con ID: %d", prodotto.getId()));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante l'aggiornamento del prodotto con ID: " + prodotto.getId());
        }
    }
}