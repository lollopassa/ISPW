package com.biteme.app.persistence.database;

import com.biteme.app.entities.Archivio;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.ArchivioDao;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseArchivioDao implements ArchivioDao {

    private static final Logger LOGGER = Logger.getLogger(DatabaseArchivioDao.class.getName());
    private final Connection connection;

    public DatabaseArchivioDao() throws DatabaseConfigurationException {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new DatabaseConfigurationException("Errore connessione database", e);
        }
    }

    @Override
    public Optional<Archivio> load(Integer id) {
        String query = "SELECT id, id_ordine, prodotti, quantita, totale, data_archiviazione FROM archivio WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToArchivio(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> String.format("Errore caricamento archivio ID: %d", id));
        }
        return Optional.empty();
    }

    @Override
    public void store(Archivio archivio) {
        String query = "INSERT INTO archivio (id_ordine, prodotti, quantita, totale, data_archiviazione) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, archivio.getIdOrdine());
            stmt.setString(2, convertListToString(archivio.getProdotti()));
            stmt.setString(3, convertListToString(archivio.getQuantita()));
            stmt.setBigDecimal(4, archivio.getTotale());
            stmt.setTimestamp(5, Timestamp.valueOf(archivio.getDataArchiviazione()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Salvataggio fallito");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    archivio.setIdOrdine(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore salvataggio archivio");
        }
    }

    @Override
    public void delete(Integer id) {
        String query = "DELETE FROM archivio WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> String.format("Errore eliminazione archivio ID: %d", id));
        }
    }

    @Override
    public boolean exists(Integer id) {
        String query = "SELECT COUNT(*) FROM archivio WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore verifica esistenza archivio");
            return false;
        }
    }

    @Override
    public List<Archivio> getAll() {
        List<Archivio> archivi = new ArrayList<>();
        String query = "SELECT id, id_ordine, prodotti, quantita, totale, data_archiviazione FROM archivio";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                archivi.add(mapResultSetToArchivio(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore recupero archivi");
        }
        return archivi;
    }

    @Override
    public List<Archivio> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Archivio> archivi = new ArrayList<>();
        String query = "SELECT id, id_ordine, prodotti, quantita, totale, data_archiviazione " +
                "FROM archivio " +
                "WHERE data_archiviazione BETWEEN ? AND ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    archivi.add(mapResultSetToArchivio(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Errore recupero archivi per intervallo di tempo");
        }
        return archivi;
    }

    private Archivio mapResultSetToArchivio(ResultSet rs) throws SQLException {
        return new Archivio(
                rs.getInt("id_ordine"),
                convertStringToList(rs.getString("prodotti")),
                convertStringToIntegerList(rs.getString("quantita")),
                rs.getBigDecimal("totale"),
                rs.getTimestamp("data_archiviazione").toLocalDateTime()
        );
    }

    private List<Integer> convertStringToIntegerList(String str) {
        return List.of(str.split(",")).stream().map(Integer::parseInt).toList();
    }

    private String convertListToString(List<?> list) {
        return String.join(",", list.stream().map(Object::toString).toList());
    }

    private List<String> convertStringToList(String str) {
        return List.of(str.split(","));
    }
}
