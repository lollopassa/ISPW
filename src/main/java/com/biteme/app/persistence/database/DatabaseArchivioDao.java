package com.biteme.app.persistence.database;

import com.biteme.app.entities.Archivio;
import com.biteme.app.entities.ArchivioRiga;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.ArchivioDao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseArchivioDao implements ArchivioDao {

    @Override
    public Optional<Archivio> read(Integer id) {
        final String sqlA =
                "SELECT totale, data_archiviazione FROM archivio WHERE id_ordine = ?";
        final String sqlR =
                "SELECT ar.quantita, p.id, p.nome, p.prezzo, p.categoria, p.disponibile "
                        + "FROM archivio_riga ar "
                        + "JOIN prodotti p ON ar.id_prodotto = p.id "
                        + "WHERE ar.id_ordine = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psA = conn.prepareStatement(sqlA);
             PreparedStatement psR = conn.prepareStatement(sqlR)) {

            psA.setInt(1, id);
            try (ResultSet rsA = psA.executeQuery()) {
                if (!rsA.next()) {
                    return Optional.empty();
                }
                var totale = rsA.getBigDecimal("totale");
                var data = rsA.getTimestamp("data_archiviazione").toLocalDateTime();

                psR.setInt(1, id);
                List<ArchivioRiga> righe = new ArrayList<>();
                try (ResultSet rsR = psR.executeQuery()) {
                    while (rsR.next()) {
                        var p = new com.biteme.app.entities.Prodotto(
                                rsR.getInt("id"),
                                rsR.getString("nome"),
                                rsR.getBigDecimal("prezzo"),
                                com.biteme.app.entities.Categoria
                                        .valueOf(rsR.getString("categoria")),
                                rsR.getBoolean("disponibile")
                        );
                        righe.add(new ArchivioRiga(p, rsR.getInt("quantita")));
                    }
                }

                return Optional.of(new Archivio(id, righe, totale, data));
            }

        } catch (SQLException ex) {
            throw new DatabaseConfigurationException(
                    "Impossibile leggere l'archivio per id " + id, ex);
        }
    }

    @Override
    public void create(Archivio arch) {
        final String upsertA =
                "INSERT INTO archivio (id_ordine, totale, data_archiviazione) VALUES (?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "  totale = VALUES(totale), "
                        + "  data_archiviazione = VALUES(data_archiviazione)";
        final String deleteR = "DELETE FROM archivio_riga WHERE id_ordine = ?";
        final String insR    =
                "INSERT INTO archivio_riga (id_ordine, id_prodotto, quantita) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psA = conn.prepareStatement(upsertA)) {
                psA.setInt(1, arch.getIdOrdine());
                psA.setBigDecimal(2, arch.getTotale());
                psA.setTimestamp(3, Timestamp.valueOf(arch.getDataArchiviazione()));
                psA.executeUpdate();
            }

            try (PreparedStatement psD = conn.prepareStatement(deleteR)) {
                psD.setInt(1, arch.getIdOrdine());
                psD.executeUpdate();
            }

            try (PreparedStatement psR = conn.prepareStatement(insR)) {
                for (var r : arch.getRighe()) {
                    int pid = r.getProdotto().getId();
                    if (pid > 0) {
                        psR.setInt(1, arch.getIdOrdine());
                        psR.setInt(2, pid);
                        psR.setInt(3, r.getQuantita());
                        psR.addBatch();
                    }
                }
                psR.executeBatch();
            }

            conn.commit();
        } catch (SQLException ex) {
            throw new DatabaseConfigurationException(
                    "Impossibile salvare o aggiornare l'archivio per id "
                            + arch.getIdOrdine(), ex);
        }
    }

    @Override
    public void delete(Integer id) {
        final String delR = "DELETE FROM archivio_riga WHERE id_ordine = ?";
        final String delA = "DELETE FROM archivio WHERE id_ordine = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psR = conn.prepareStatement(delR);
             PreparedStatement psA = conn.prepareStatement(delA)) {

            psR.setInt(1, id);
            psR.executeUpdate();

            psA.setInt(1, id);
            psA.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabaseConfigurationException(
                    "Impossibile eliminare l'archivio per id " + id, ex);
        }
    }

    @Override
    public boolean exists(Integer id) {
        final String sql = "SELECT COUNT(*) FROM archivio WHERE id_ordine = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new DatabaseConfigurationException(
                    "Impossibile verificare l'esistenza dell'archivio per id " + id, ex);
        }
    }

    @Override
    public List<Archivio> getAll() {
        final String sql = "SELECT id_ordine FROM archivio";
        var result = new ArrayList<Archivio>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id_ordine");
                read(id).ifPresent(result::add);
            }
            return result;
        } catch (SQLException ex) {
            throw new DatabaseConfigurationException(
                    "Impossibile recuperare tutti gli archivi", ex);
        }
    }

    @Override
    public List<Archivio> findByDateRange(LocalDateTime start, LocalDateTime end) {
        final String sql =
                "SELECT id_ordine FROM archivio WHERE data_archiviazione BETWEEN ? AND ?";
        var result = new ArrayList<Archivio>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id_ordine");
                    read(id).ifPresent(result::add);
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new DatabaseConfigurationException(
                    "Impossibile cercare archivi nel range " + start + " - " + end, ex);
        }
    }
}
