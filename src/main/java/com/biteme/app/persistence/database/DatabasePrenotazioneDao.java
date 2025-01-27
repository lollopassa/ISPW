package com.biteme.app.persistence.database;

import com.biteme.app.entity.Prenotazione;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.PrenotazioneDao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabasePrenotazioneDao implements PrenotazioneDao {

    private static final Logger LOGGER = Logger.getLogger(DatabasePrenotazioneDao.class.getName());
    private final Connection connection;

    public DatabasePrenotazioneDao() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new DatabaseConfigurationException("Impossibile connettersi al database a causa di una configurazione errata", e);
        }
    }

    @Override
    public Optional<Prenotazione> load(Integer id) {
        String query = "SELECT id, nomeCliente, orario, data, note, telefono, coperti FROM prenotazione WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPrenotazione(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante il caricamento della prenotazione con ID: " + id);
        }
        return Optional.empty();
    }

    @Override
    public void store(Prenotazione entity) {
        String query = "INSERT INTO prenotazione (nomeCliente, orario, data, note, telefono, coperti) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getNomeCliente());
            stmt.setTime(2, Time.valueOf(entity.getOrario()));
            stmt.setDate(3, Date.valueOf(entity.getData()));
            stmt.setString(4, entity.getNote());
            stmt.setString(5, entity.getTelefono());
            stmt.setInt(6, entity.getCoperti());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento della prenotazione fallito, nessuna riga aggiunta.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Inserimento della prenotazione fallito, nessun ID generato.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante il salvataggio della prenotazione");
        }
    }

    @Override
    public void delete(Integer id) {
        String query = "DELETE FROM prenotazione WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, () -> "Nessuna prenotazione trovata con ID: " + id);
            } else {
                LOGGER.log(Level.INFO, () -> "Prenotazione con ID: " + id + " eliminata con successo");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante l'eliminazione della prenotazione con ID: " + id);
        }
    }

    @Override
    public boolean exists(Integer id) {
        String query = "SELECT COUNT(*) FROM prenotazione WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore durante la verifica dell'esistenza della prenotazione con ID: " + id);
            return false;
        }
    }

    @Override
    public List<Prenotazione> getByOrario(LocalTime orario) {
        String query = "SELECT id, nomeCliente, orario, data, note, telefono, coperti FROM prenotazione WHERE orario = ?";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTime(1, Time.valueOf(orario));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(mapResultSetToPrenotazione(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> String.format("Errore durante il recupero delle prenotazioni per l'orario: %s", orario));
        }
        return prenotazioni;
    }

    @Override
    public List<Prenotazione> getByData(LocalDate data) {
        String query = "SELECT id, nomeCliente, orario, data, note, telefono, coperti FROM prenotazione WHERE data = ?";
        List<Prenotazione> prenotazioni = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(data));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prenotazioni.add(mapResultSetToPrenotazione(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> String.format("Errore durante il recupero delle prenotazioni per la data: %s", data));
        }
        return prenotazioni;
    }

    private Prenotazione mapResultSetToPrenotazione(ResultSet rs) throws SQLException {
        return new Prenotazione(
                rs.getInt("id"),
                rs.getString("nomeCliente"),
                rs.getTime("orario").toLocalTime(),
                rs.getDate("data").toLocalDate(),
                rs.getString("note"),
                rs.getString("telefono"),
                rs.getInt("coperti")
        );
    }
}