package com.biteme.app.persistence.txt;

import com.biteme.app.entities.Ordine;
import com.biteme.app.persistence.OrdineDao;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TxtOrdineDao implements OrdineDao {
    private static final Logger LOGGER = Logger.getLogger(TxtOrdineDao.class.getName());
    private static final String FILE_PATH = "data/ordini.txt";
    private static final String DELIM = "\\|";
    private static final String DELIM_OUT = "|";
    private static final String EMPTY = "EMPTY";

    private final List<Ordine> ordini;
    private int currentId;

    public TxtOrdineDao() {
        ordini = loadFromFile();
        currentId = ordini.stream().mapToInt(Ordine::getId).max().orElse(0) + 1;
    }

    private List<Ordine> loadFromFile() {
        List<Ordine> list = new ArrayList<>();
        try {
            Files.createDirectories(Paths.get("data"));
            File f = new File(FILE_PATH);
            if (!f.exists()) return list;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Ordine o = deserialize(line);
                    if (o != null) list.add(o);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore file txt ordini", e);
        }
        return list;
    }

    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Ordine o : ordini) {
                bw.write(serialize(o));
                bw.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore scrittura file ordini", e);
        }
    }

    @Override
    public Optional<Ordine> load(Integer id) {
        return ordini.stream().filter(o -> o.getId() == id).findFirst();
    }

    @Override
    public void store(Ordine ordine) {
        if (ordine.getId() > 0) delete(ordine.getId());
        else ordine.setId(currentId++);
        ordini.add(ordine);
        saveToFile();
    }

    @Override
    public void delete(Integer id) {
        ordini.removeIf(o -> o.getId() == id);
        saveToFile();
    }

    @Override
    public boolean exists(Integer id) {
        return ordini.stream().anyMatch(o -> o.getId() == id);
    }

    @Override
    public Ordine getById(Integer id) {
        return load(id).orElseThrow(() -> new IllegalArgumentException("Ordine con ID " + id + " non trovato"));
    }

    private String serialize(Ordine o) {
        String prodStr = o.getProdotti().isEmpty() ? EMPTY : String.join(",", o.getProdotti());
        String qtyStr = o.getQuantita().isEmpty() ? EMPTY : o.getQuantita().stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        String prezStr = o.getPrezzi() == null || o.getPrezzi().isEmpty()
                ? EMPTY
                : o.getPrezzi().stream()
                .map(BigDecimal::toString)
                .collect(Collectors.joining(","));
        return o.getId() + DELIM_OUT + prodStr + DELIM_OUT + qtyStr + DELIM_OUT + prezStr;
    }

    private Ordine deserialize(String line) {
        try {
            String[] p = line.split(DELIM);
            if (p.length != 4) return null;

            int id = Integer.parseInt(p[0]);

            List<String> prod = p[1].equals(EMPTY)
                    ? List.of()
                    : List.of(p[1].split(","));

            List<Integer> qty = p[2].equals(EMPTY)
                    ? List.of()
                    : Arrays.stream(p[2].split(","))
                    .map(Integer::parseInt)
                    .toList();

            List<BigDecimal> prez = p[3].equals(EMPTY)
                    ? List.of()
                    : Arrays.stream(p[3].split(","))
                    .map(BigDecimal::new)
                    .toList();

            return new Ordine(id, prod, qty, prez);
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, () -> "Errore deserializza ordine: " + line);
            }
            return null;
        }
    }

}