package com.biteme.app.persistence.database;

import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.entities.TipoOrdinazione;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.OrdinazioneDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseOrdinazioneDao implements OrdinazioneDao {

    private static final Logger LOGGER = Logger.getLogger(DatabaseOrdinazioneDao.class.getName());

    /* ============ CREATE ============ */
    @Override
    public int create(Ordinazione o) {
        final String sql = """
            INSERT INTO ordinazione
            (nomeCliente, numeroClienti, tipoOrdine,
             infoTavolo, statoOrdine, orarioCreazione)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, o.getNomeCliente());
            ps.setString(2, o.getNumeroClienti());
            ps.setString(3, o.getTipoOrdine().name());
            ps.setString(4, o.getInfoTavolo());
            ps.setString(5, o.getStatoOrdine().name());
            ps.setString(6, o.getOrarioCreazione());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("ID non generato");
            }

        } catch (SQLException ex) {
            throw new DatabaseConfigurationException("Errore durante il salvataggio dell'ordinazione", ex);
        }
    }

    /* ============ READ ============ */
    @Override
    public Optional<Ordinazione> read(int id) {
        final String sql = """
            SELECT id, nomeCliente, numeroClienti, tipoOrdine,
                   infoTavolo, statoOrdine, orarioCreazione
            FROM ordinazione
            WHERE id = ?
            """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex,
                    () -> "Errore durante il caricamento dell'ordinazione con ID: " + id);
            return Optional.empty();
        }
    }

    /* ============ DELETE ============ */
    @Override
    public void delete(int id) {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement delOrdine =
                         c.prepareStatement("DELETE FROM ordine WHERE id=?");
                 PreparedStatement delOrdinazione =
                         c.prepareStatement("DELETE FROM ordinazione WHERE id=?")) {

                delOrdine.setInt(1, id);
                delOrdine.executeUpdate();

                delOrdinazione.setInt(1, id);
                int rows = delOrdinazione.executeUpdate();

                if (rows == 0) {
                    LOGGER.warning(() -> "Nessuna ordinazione trovata con ID: " + id);
                }
            }
            c.commit();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex,
                    () -> "Errore durante l'eliminazione dell'ordinazione con ID: " + id);
        }
    }

    /* ============ EXISTS: eredita default ============ */

    /* ============ LISTA COMPLETA ============ */
    @Override
    public List<Ordinazione> getAll() {
        final String sql = """
            SELECT id, nomeCliente, numeroClienti, tipoOrdine,
                   infoTavolo, statoOrdine, orarioCreazione
            FROM ordinazione
            """;
        List<Ordinazione> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex, () -> "Errore durante il recupero di tutti gli ordini");
        }
        return list;
    }

    /* ============ UPDATE STATO ============ */
    @Override
    public void aggiornaStato(int id, StatoOrdinazione nuovo) {
        final String sql = "UPDATE ordinazione SET statoOrdine=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nuovo.name());
            ps.setInt(2, id);
            if (ps.executeUpdate() == 0) {
                LOGGER.warning(() -> "Nessuna ordinazione trovata con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new DatabaseConfigurationException("Errore nell'aggiornamento stato", ex);
        }
    }

    /* ============ mapper helper ============ */
    private Ordinazione map(ResultSet rs) throws SQLException {
        return new Ordinazione(
                rs.getInt("id"),
                rs.getString("nomeCliente"),
                rs.getString("numeroClienti"),
                TipoOrdinazione.valueOf(rs.getString("tipoOrdine").toUpperCase()),
                rs.getString("infoTavolo"),
                StatoOrdinazione.valueOf(rs.getString("statoOrdine").toUpperCase()),
                rs.getString("orarioCreazione")
        );
    }
}
