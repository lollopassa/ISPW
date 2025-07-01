package com.biteme.app.persistence.txt;

import com.biteme.app.entities.Archivio;
import com.biteme.app.entities.ArchivioRiga;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.persistence.ArchivioDao;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TxtArchivioDao implements ArchivioDao {
    private static final String FILE = "data/archivi.txt";
    private static final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final List<Archivio> storage;

    public TxtArchivioDao() {
        storage = load();
    }

    private List<Archivio> load() {
        try {
            Files.createDirectories(Paths.get("data"));
            if (!Files.exists(Paths.get(FILE))) return new ArrayList<>();

            var list = new ArrayList<Archivio>();
            try (var br = Files.newBufferedReader(Paths.get(FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|", 4);
                    int id = Integer.parseInt(parts[0]);
                    LocalDateTime dt = LocalDateTime.parse(parts[1], fmt);
                    BigDecimal tot = new BigDecimal(parts[2]);

                    var righe = new ArrayList<ArchivioRiga>();
                    if (parts.length == 4 && !parts[3].isEmpty()) {
                        for (var rec : parts[3].split(";")) {
                            var f = rec.split(",", 4);
                            int pid = Integer.parseInt(f[0].trim());
                            String nm = f[1].trim();
                            BigDecimal pr = new BigDecimal(f[2].trim());
                            int qt = Integer.parseInt(f[3].trim());
                            // placeholder getId <= 0 verrà filtrato in create()
                            righe.add(new ArchivioRiga(new Prodotto(pid, nm, pr, null, true), qt));
                        }
                    }
                    list.add(new Archivio(id, righe, tot, dt));
                }
            }
            return list;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void save() {
        try (var bw = Files.newBufferedWriter(Paths.get(FILE))) {
            for (var a : storage) {
                var sb = new StringBuilder()
                        .append(a.getIdOrdine()).append("|")
                        .append(a.getDataArchiviazione().format(fmt)).append("|")
                        .append(a.getTotale()).append("|");

                // ** Solo righe con id > 0 **
                String rows = a.getRighe().stream()
                        .filter(r -> r.getProdotto().getId() > 0)
                        .map(r -> {
                            var p = r.getProdotto();
                            return p.getId() + "," +
                                    p.getNome() + "," +
                                    p.getPrezzo() + "," +
                                    r.getQuantita();
                        })
                        .collect(Collectors.joining(";"));

                sb.append(rows);
                bw.write(sb.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<Archivio> read(Integer id) {
        return storage.stream()
                .filter(a -> a.getIdOrdine().equals(id))
                .findFirst();
    }

    @Override
    public void create(Archivio a) {
        // Rimuovo esistenti
        storage.removeIf(x -> x.getIdOrdine().equals(a.getIdOrdine()));

        // Filtro righe placeholder
        var righeValide = a.getRighe().stream()
                .filter(r -> r.getProdotto().getId() > 0)
                .toList();

        storage.add(new Archivio(
                a.getIdOrdine(),
                righeValide,
                a.getTotale(),
                a.getDataArchiviazione()
        ));
        save();
    }

    @Override
    public void delete(Integer id) {
        storage.removeIf(x -> x.getIdOrdine().equals(id));
        save();
    }

    @Override
    public boolean exists(Integer id) {
        return storage.stream().anyMatch(a -> a.getIdOrdine().equals(id));
    }

    @Override
    public List<Archivio> getAll() {
        return List.copyOf(storage);
    }

    @Override
    public List<Archivio> findByDateRange(LocalDateTime s, LocalDateTime e) {
        var out = new ArrayList<Archivio>();
        for (var a : storage) {
            var d = a.getDataArchiviazione();
            if (!d.isBefore(s) && !d.isAfter(e)) {
                out.add(a);
            }
        }
        return out;
    }
}
