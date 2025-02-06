package com.biteme.app.persistence.database;

import com.biteme.app.model.Ordinazione;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.model.StatoOrdine;
import com.biteme.app.model.TipoOrdine;

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
    public Optional<Ordinazione> load(Integer id) {
        String query = "SELECT id, nomeCliente, numeroClienti, tipoOrdine, infoTavolo, statoOrdine, orarioCreazione FROM ordinazione WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrdinazione(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante il caricamento dell'ordinazione con ID: " + id);
        }
        return Optional.empty();
    }

    @Override
    public void store(Ordinazione ordinazione) {
        String query = "INSERT INTO ordinazione (nomeCliente, numeroClienti, tipoOrdine, infoTavolo, statoOrdine, orarioCreazione) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ordinazione.getNomeCliente());
            stmt.setString(2, ordinazione.getNumeroClienti() == null ? null : ordinazione.getNumeroClienti());
            stmt.setString(3, ordinazione.getTipoOrdine().name());
            stmt.setString(4, ordinazione.getInfoTavolo() == null ? null : ordinazione.getInfoTavolo());
            stmt.setString(5, ordinazione.getStatoOrdine().name()); // Usa .name() per la stringa
            stmt.setString(6, ordinazione.getOrarioCreazione() == null ? null : ordinazione.getOrarioCreazione());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento dell'ordinazione fallito, nessuna riga aggiunta.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ordinazione.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Inserimento dell'ordinazione fallito, nessun ID generato.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante il salvataggio dell'ordinazione");
        }
    }

    @Override
    public void delete(Integer id) {
        // Prima, elimina le righe correlate nella tabella 'ordine'
        String deleteFromOrdine = "DELETE FROM ordine WHERE id = ?";
        String deleteFromOrdinazione = "DELETE FROM ordinazione WHERE id = ?";

        try {
            connection.setAutoCommit(false); // Disabilita il commit automatico per controllare la transazione

            // Step 1: Elimina riferimenti correlati nella tabella figlia
            try (PreparedStatement stmtOrdine = connection.prepareStatement(deleteFromOrdine)) {
                stmtOrdine.setInt(1, id);
                stmtOrdine.executeUpdate();
            }

            // Step 2: Elimina l'ordinazione dalla tabella principale
            try (PreparedStatement stmtOrdinazione = connection.prepareStatement(deleteFromOrdinazione)) {
                stmtOrdinazione.setInt(1, id);
                int affectedRows = stmtOrdinazione.executeUpdate();

                if (affectedRows == 0) {
                    LOGGER.log(Level.WARNING, () -> "Nessun ordinazione trovato con ID: " + id);
                } else {
                    LOGGER.log(Level.INFO, () -> "Ordinazione con ID: " + id + " eliminato con successo");
                }
            }

            connection.commit(); // Conferma la transazione
        } catch (SQLException e) {
            try {
                connection.rollback(); // Esegui un rollback in caso di errore
                LOGGER.log(Level.SEVERE, "Transazione rollback a causa di un errore: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Errore durante il rollback: " + rollbackEx.getMessage());
            }
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante l'eliminazione dell'ordinazione con ID: " + id);
        } finally {
            try {
                connection.setAutoCommit(true); // Ripristina il commit automatico
            } catch (SQLException autoCommitEx) {
                LOGGER.log(Level.SEVERE, "Errore durante il ripristino del commit automatico: " + autoCommitEx.getMessage());
            }
        }
    }

    @Override
    public boolean exists(Integer id) {
        String query = "SELECT COUNT(*) FROM ordinazione WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante la verifica dell'esistenza dell'ordinazione con ID: " + id);
            return false;
        }
    }

    @Override
    public List<Ordinazione> getAll() {
        String query = "SELECT id, nomeCliente, numeroClienti, tipoOrdine, infoTavolo, statoOrdine, orarioCreazione FROM ordinazione";
        List<Ordinazione> ordini = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ordini.add(mapResultSetToOrdinazione(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante il recupero di tutti gli ordini.");
        }
        return ordini;
    }

    @Override
    public void aggiornaStato(int id, StatoOrdine nuovoStato) {
        String query = "UPDATE Ordinazione SET statoOrdine = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, nuovoStato.name()); // Converte lo stato in stringa
            preparedStatement.setInt(2, id); // Imposta l'ID dell'ordinazione

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated == 0) {
                LOGGER.log(Level.WARNING, "Nessuna ordinazione trovata con ID: {0}", id);            }
        } catch (SQLException e) {
            throw new DatabaseConfigurationException("Errore durante l'aggiornamento dello stato dell'ordinazione.", e);
        }
    }

    private Ordinazione mapResultSetToOrdinazione(ResultSet rs) throws SQLException {
        return new Ordinazione(
                rs.getInt("id"),
                rs.getString("nomeCliente"),
                rs.getString("numeroClienti"),
                TipoOrdine.valueOf(rs.getString("tipoOrdine").toUpperCase()),
                rs.getString("infoTavolo"),
                StatoOrdine.valueOf(rs.getString("statoOrdine").toUpperCase()), // Conversione a enum
                rs.getString("orarioCreazione")
        );
    }
}