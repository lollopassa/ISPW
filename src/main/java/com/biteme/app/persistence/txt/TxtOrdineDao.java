package com.biteme.app.persistence.txt;

import com.biteme.app.entities.Ordine;
import com.biteme.app.persistence.OrdineDao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TxtOrdineDao implements OrdineDao {
    private static final Logger LOGGER = Logger.getLogger(TxtOrdineDao.class.getName());
    private final List<Ordine> ordini;
    private int currentId;
    private static final String EMPTY = "EMPTY";
    private static final String FILE_PATH = "data/ordini.txt";
    private static final String DELIMITER = "\\|";
    private static final String DELIMITER_OUT = "|";

    public TxtOrdineDao() {
        ordini = loadFromFile();
        currentId = calculateCurrentId();
    }

    private List<Ordine> loadFromFile() {
        List<Ordine> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Ordine o = deserialize(line);
                if (o != null) list.add(o);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento degli ordini", e);
        }
        return list;
    }

    private void saveToFile() {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nella creazione della directory", e);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Ordine o : ordini) {
                bw.write(serialize(o));
                bw.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio degli ordini", e);
        }
    }

    private int calculateCurrentId() {
        File idFile = new File("data/last-id.txt");
        if (idFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(idFile))) {
                return Integer.parseInt(br.readLine());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Errore nella lettura dell'ID", e);
            }
        }
        return ordini.stream()
                .mapToInt(Ordine::getId)
                .max()
                .orElse(0) + 1;
    }

    private void saveCurrentId() {
        File idFile = new File("data/last-id.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(idFile))) {
            bw.write(String.valueOf(currentId));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio dell'ID", e);
        }
    }

    @Override
    public Optional<Ordine> load(Integer id) {
        return ordini.stream().filter(o -> o.getId() == id).findFirst();
    }

    @Override
    public void store(Ordine ordine) {
        if (ordine.getId() > 0) {
            delete(ordine.getId());
        } else {
            ordine.setId(currentId++);
            saveCurrentId();
        }
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
        String prodottiStr = o.getProdotti().isEmpty() ? EMPTY : String.join(",", o.getProdotti());
        String quantitaStr = o.getQuantita().isEmpty() ? EMPTY :
                o.getQuantita().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));
        return o.getId() + DELIMITER_OUT + prodottiStr + DELIMITER_OUT + quantitaStr;
    }

    private Ordine deserialize(String line) {
        try {
            String[] parts = line.split(DELIMITER);
            if (parts.length != 3) {
                return null;
            }

            int id = Integer.parseInt(parts[0]);
            List<String> prodotti = parts[1].equals(EMPTY) ? List.of() : Arrays.asList(parts[1].split(","));
            List<Integer> quantita = parts[2].equals(EMPTY) ? List.of() :
                    Arrays.stream(parts[2].split(","))
                            .map(Integer::parseInt)
                            .toList();

            return new Ordine(id, prodotti, quantita);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Errore nel formato numerico: {0}", line);
            return null;
        }
    }
}