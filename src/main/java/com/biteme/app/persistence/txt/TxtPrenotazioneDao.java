package com.biteme.app.persistence.txt;

import com.biteme.app.entities.Prenotazione;
import com.biteme.app.persistence.PrenotazioneDao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TxtPrenotazioneDao implements PrenotazioneDao {
    private static final Logger LOGGER = Logger.getLogger(TxtPrenotazioneDao.class.getName());
    private final List<Prenotazione> prenotazioni;
    private int currentId;
    private static final String FILE_PATH = "data/prenotazioni.txt";
    private static final String DELIMITER = "\\|";
    private static final String DELIMITER_OUT = "|";

    public TxtPrenotazioneDao() {
        prenotazioni = loadFromFile();
        currentId = calculateCurrentId();
    }

    private void saveToFile() {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nella creazione della directory", e);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Prenotazione p : prenotazioni) {
                bw.write(serialize(p));
                bw.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio delle prenotazioni", e);
        }
    }

    private int calculateCurrentId() {
        return prenotazioni.stream()
                .mapToInt(Prenotazione::getId)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public Optional<Prenotazione> load(Integer id) {
        return prenotazioni.stream().filter(p -> p.getId() == id).findFirst();
    }

    private List<Prenotazione> loadFromFile() {
        List<Prenotazione> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Prenotazione p = deserialize(line);
                if (p != null) list.add(p);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento delle prenotazioni", e);
        }
        return list;
    }

    @Override
    public void store(Prenotazione prenotazione) {
        if (prenotazione.getId() > 0) {
            delete(prenotazione.getId());
        } else {
            prenotazione.setId(currentId++);
        }
        prenotazioni.add(prenotazione);
        saveToFile();
    }

    @Override
    public void delete(Integer id) {
        prenotazioni.removeIf(p -> p.getId() == id);
        saveToFile();
    }

    @Override
    public boolean exists(Integer id) {
        return prenotazioni.stream().anyMatch(p -> p.getId() == id);
    }

    @Override
    public boolean existsDuplicate(Prenotazione p) {
        return prenotazioni.stream().anyMatch(x ->
                x.getNomeCliente().equalsIgnoreCase(p.getNomeCliente()) &&
                        x.getOrario().equals(p.getOrario()) &&
                        x.getData().equals(p.getData()) &&
                        x.getCoperti() == p.getCoperti()
        );
    }

    @Override
    public List<Prenotazione> getByData(LocalDate data) {
        return prenotazioni.stream()
                .filter(p -> p.getData().equals(data))
                .toList();
    }

    @Override
    public void update(Prenotazione prenotazione) {
        delete(prenotazione.getId());
        prenotazioni.add(prenotazione);
        saveToFile();
    }

    private String serialize(Prenotazione p) {
        String dataStr = p.getData().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return p.getId() + DELIMITER_OUT +
                p.getNomeCliente() + DELIMITER_OUT +
                p.getOrario() + DELIMITER_OUT +
                dataStr + DELIMITER_OUT +
                (p.getNote() == null ? "" : p.getNote()) + DELIMITER_OUT +
                (p.getEmail() == null ? "" : p.getEmail()) + DELIMITER_OUT +
                p.getCoperti();
    }

    private Prenotazione deserialize(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length != 7) {
            return null;
        }
        try {
            int id = Integer.parseInt(parts[0]);
            String nomeCliente = parts[1];
            LocalTime orario = LocalTime.parse(parts[2]);
            LocalDate data = LocalDate.parse(parts[3], DateTimeFormatter.ISO_LOCAL_DATE);
            String note = parts[4];
            String email = parts[5];
            int coperti = Integer.parseInt(parts[6]);
            return new Prenotazione(id, nomeCliente, orario, data, note, email, coperti);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Errore nella deserializzazione della riga: {0}", line);
            return null;
        }
    }
}