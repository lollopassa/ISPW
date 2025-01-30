package com.biteme.app.persistence.database;

import com.biteme.app.entity.Ordine;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.entity.TipoOrdine;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DatabaseOrdinazioneDao implements OrdinazioneDao {

    private static final Logger LOGGER = Logger.getLogger(DatabaseOrdinazioneDao.class.getName());
    private final Connection connection;

    public DatabaseOrdinazioneDao() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new DatabaseConfigurationException("Impossibile connettersi al database a causa di una configurazione errata", e);
        }
    }

    @Override
    public Optional<Ordine> load(Integer id) {
        String query = "SELECT id, nomeCliente, numeroClienti, tipoOrdine, infoTavolo, statoOrdine, orarioCreazione FROM ordine WHERE id = ?";
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
        String query = "INSERT INTO ordine (nomeCliente, numeroClienti, tipoOrdine, infoTavolo, statoOrdine, orarioCreazione) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ordine.getNomeCliente());
            stmt.setString(2, ordine.getNumeroClienti() == null ? null : ordine.getNumeroClienti());
            stmt.setString(3, ordine.getTipoOrdine().name());
            stmt.setString(4, ordine.getInfoTavolo() == null ? null : ordine.getInfoTavolo());
            stmt.setString(5, ordine.getStatoOrdine() == null ? null : ordine.getStatoOrdine());
            stmt.setString(6, ordine.getOrarioCreazione() == null ? null : ordine.getOrarioCreazione());

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

    @Override
    public List<Ordine> getAll() {
        String query = "SELECT id, nomeCliente, numeroClienti, tipoOrdine, infoTavolo, statoOrdine, orarioCreazione FROM ordine";
        List<Ordine> ordini = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ordini.add(mapResultSetToOrdine(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante il recupero di tutti gli ordini.");
        }
        return ordini;
    }

    private Ordine mapResultSetToOrdine(ResultSet rs) throws SQLException {
        return new Ordine(
                rs.getInt("id"),
                rs.getString("nomeCliente"),
                rs.getString("numeroClienti"),
                TipoOrdine.valueOf(rs.getString("tipoOrdine").toUpperCase()),
                rs.getString("infoTavolo"),
                rs.getString("statoOrdine"),
                rs.getString("orarioCreazione") // Ora Ã¨ gestito come String
        );
    }
}