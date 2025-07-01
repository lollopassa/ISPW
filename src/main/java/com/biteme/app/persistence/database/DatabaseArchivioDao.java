package com.biteme.app.persistence.database;

import com.biteme.app.entities.Archivio;
import com.biteme.app.entities.ArchivioRiga;
import com.biteme.app.entities.Categoria;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.exception.DatabaseConfigurationException;
import com.biteme.app.persistence.ArchivioDao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DatabaseArchivioDao implements ArchivioDao {

    private final Connection conn;

    public DatabaseArchivioDao() throws DatabaseConfigurationException {
        try { this.conn = DatabaseConnection.getConnection(); }
        catch (SQLException e) { throw new DatabaseConfigurationException(e.getMessage(), e); }
    }

    @Override
    public Optional<Archivio> read(Integer id) {
        String sqlA = "SELECT totale, data_archiviazione FROM archivio WHERE id_ordine=?";
        String sqlR = "SELECT ar.quantita, p.id, p.nome, p.prezzo, p.categoria, p.disponibile "
                + "FROM archivio_riga ar JOIN prodotti p ON ar.id_prodotto=p.id "
                + "WHERE ar.id_ordine=?";
        try (PreparedStatement psA = conn.prepareStatement(sqlA);
             PreparedStatement psR = conn.prepareStatement(sqlR)) {

            psA.setInt(1, id);
            try (ResultSet rsA = psA.executeQuery()) {
                if (!rsA.next()) return Optional.empty();
                BigDecimal tot = rsA.getBigDecimal("totale");
                LocalDateTime dt = rsA.getTimestamp("data_archiviazione").toLocalDateTime();

                psR.setInt(1, id);
                List<ArchivioRiga> righe = new ArrayList<>();
                try (ResultSet rsR = psR.executeQuery()) {
                    while (rsR.next()) {
                        Prodotto p = new Prodotto(
                                rsR.getInt("id"),
                                rsR.getString("nome"),
                                rsR.getBigDecimal("prezzo"),
                                Categoria.valueOf(rsR.getString("categoria")),
                                rsR.getBoolean("disponibile")
                        );
                        righe.add(new ArchivioRiga(p, rsR.getInt("quantita")));
                    }
                }

                return Optional.of(new Archivio(id, righe, tot, dt));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void create(Archivio arch) {
        String insA = "INSERT INTO archivio (id_ordine, totale, data_archiviazione) VALUES (?,?,?)";
        String insR = "INSERT INTO archivio_riga (id_ordine,id_prodotto,quantita) VALUES (?,?,?)";
        try (PreparedStatement psA = conn.prepareStatement(insA);
             PreparedStatement psR = conn.prepareStatement(insR)) {

            psA.setInt(1, arch.getIdOrdine());
            psA.setBigDecimal(2, arch.getTotale());
            psA.setTimestamp(3, Timestamp.valueOf(arch.getDataArchiviazione()));
            psA.executeUpdate();

            for (ArchivioRiga r : arch.getRighe()) {
                psR.setInt(1, arch.getIdOrdine());
                psR.setLong(2, r.getProdotto().getId());
                psR.setInt(3, r.getQuantita());
                psR.addBatch();
            }
            psR.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement psR = conn.prepareStatement("DELETE FROM archivio_riga WHERE id_ordine=?");
             PreparedStatement psA = conn.prepareStatement("DELETE FROM archivio WHERE id_ordine=?")) {
            psR.setInt(1, id); psR.executeUpdate();
            psA.setInt(1, id); psA.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(Integer id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM archivio WHERE id_ordine=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Archivio> getAll() {
        List<Archivio> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_ordine FROM archivio")) {
            while (rs.next()) {
                read(rs.getInt("id_ordine")).ifPresent(list::add);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Archivio> findByDateRange(LocalDateTime s, LocalDateTime e) {
        List<Archivio> list = new ArrayList<>();
        String sql = "SELECT id_ordine FROM archivio WHERE data_archiviazione BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(s));
            ps.setTimestamp(2, Timestamp.valueOf(e));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) read(rs.getInt("id_ordine")).ifPresent(list::add);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }
}
