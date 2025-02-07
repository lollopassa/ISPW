package com.biteme.app.persistence.txt;

import com.biteme.app.model.Archivio;
import com.biteme.app.persistence.ArchivioDao;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TxtArchivioDao implements ArchivioDao {
    private static final Logger LOGGER = Logger.getLogger(TxtArchivioDao.class.getName());
    private final List<Archivio> archivi;
    private int currentId;
    private static final String FILE_PATH = "data/archivi.txt";
    private static final String DELIMITER = "\\|";
    private static final String DELIMITER_OUT = "|";

    public TxtArchivioDao() {
        archivi = loadFromFile();
        currentId = calculateCurrentId();
    }

    private List<Archivio> loadFromFile() {
        List<Archivio> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return list;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Archivio a = deserialize(line);
                if (a != null) {
                    list.add(a);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento degli archivi", e);
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
            for (Archivio a : archivi) {
                bw.write(serialize(a));
                bw.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio degli archivi", e);
        }
    }

    private int calculateCurrentId() {
        return archivi.stream()
                .mapToInt(Archivio::getIdOrdine)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public Optional<Archivio> load(Integer id) {
        return archivi.stream()
                .filter(a -> a.getIdOrdine() == id)
                .findFirst();
    }

    @Override
    public void store(Archivio archivio) {
        if (archivio.getIdOrdine() == 0) {
            archivio.setIdOrdine(currentId++);
        } else {
            delete(archivio.getIdOrdine());
        }
        archivi.add(archivio);
        saveToFile();
    }

    @Override
    public void delete(Integer id) {
        archivi.removeIf(a -> a.getIdOrdine() == id);
        saveToFile();
    }

    @Override
    public boolean exists(Integer id) {
        return archivi.stream().anyMatch(a -> a.getIdOrdine() == id);
    }

    @Override
    public List<Archivio> getAll() {
        return new ArrayList<>(archivi);
    }

    @Override
    public List<Archivio> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return archivi.stream()
                .filter(a -> !a.getDataArchiviazione().isBefore(startDate) && !a.getDataArchiviazione().isAfter(endDate))
                .toList();
    }

    private String serialize(Archivio a) {
        String prodottiStr = String.join(",", a.getProdotti());
        String quantitaStr = a.getQuantita().stream().map(Object::toString).collect(Collectors.joining(","));
        String totaleStr = a.getTotale().toPlainString();
        String dataStr = a.getDataArchiviazione().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return a.getIdOrdine() + DELIMITER_OUT + prodottiStr + DELIMITER_OUT + quantitaStr + DELIMITER_OUT + totaleStr + DELIMITER_OUT + dataStr;
    }

    private Archivio deserialize(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length != 5) {
            return null;
        }
        try {
            int idOrdine = Integer.parseInt(parts[0]);
            List<String> prodotti = parts[1].isEmpty() ? List.of() : Arrays.asList(parts[1].split(","));
            List<Integer> quantita = parts[2].isEmpty() ? List.of() :
                    Arrays.stream(parts[2].split(","))
                            .map(Integer::parseInt)
                            .toList();
            BigDecimal totale = new BigDecimal(parts[3]);
            LocalDateTime dataArchiviazione = LocalDateTime.parse(parts[4], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return new Archivio(idOrdine, prodotti, quantita, totale, dataArchiviazione);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Errore nella deserializzazione della riga: {0}", line);
            return null;
        }
    }
}