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

            List<Archivio> list = new ArrayList<>();
            try (BufferedReader br = Files.newBufferedReader(Paths.get(FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // formato: idOrdine|data|totale|id, nome, prezzo, quant;...
                    String[] parts = line.split("\\|", 4);
                    int id = Integer.parseInt(parts[0]);
                    LocalDateTime dt = LocalDateTime.parse(parts[1], fmt);
                    BigDecimal tot = new BigDecimal(parts[2]);

                    List<ArchivioRiga> righe = new ArrayList<>();
                    if (parts.length == 4 && !parts[3].isEmpty()) {
                        String[] recs = parts[3].split(";");
                        for (String rec : recs) {
                            String[] f = rec.split(",", 4);
                            int pid    = Integer.parseInt(f[0].trim());
                            String nm  = f[1].trim();
                            BigDecimal pr = new BigDecimal(f[2].trim());
                            int qt    = Integer.parseInt(f[3].trim());
                            Prodotto p = new Prodotto(pid, nm, pr, null, true);
                            righe.add(new ArchivioRiga(p, qt));
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
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(FILE))) {
            for (Archivio a : storage) {
                StringBuilder sb = new StringBuilder();
                sb.append(a.getIdOrdine())
                        .append("|")
                        .append(a.getDataArchiviazione().format(fmt))
                        .append("|")
                        .append(a.getTotale())
                        .append("|");
                List<String> recs = new ArrayList<>();
                for (ArchivioRiga r : a.getRighe()) {
                    var p = r.getProdotto();
                    recs.add(p.getId() + "," + p.getNome() + "," + p.getPrezzo() + "," + r.getQuantita());
                }
                sb.append(String.join(";", recs));
                bw.write(sb.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override public Optional<Archivio> read(Integer id)         { return storage.stream().filter(a->a.getIdOrdine()==id).findFirst(); }
    @Override public void create(Archivio a) { storage.removeIf(x->x.getIdOrdine()==a.getIdOrdine()); storage.add(a); save(); }
    @Override public void delete(Integer id) { storage.removeIf(x->x.getIdOrdine()==id); save(); }
    @Override public boolean exists(Integer id) { return storage.stream().anyMatch(a->a.getIdOrdine()==id); }
    @Override public List<Archivio> getAll() { return new ArrayList<>(storage); }
    @Override public List<Archivio> findByDateRange(LocalDateTime s, LocalDateTime e) {
        List<Archivio> out = new ArrayList<>();
        for (Archivio a : storage) {
            LocalDateTime d = a.getDataArchiviazione();
            if (!d.isBefore(s) && !d.isAfter(e)) out.add(a);
        }
        return out;
    }
}
