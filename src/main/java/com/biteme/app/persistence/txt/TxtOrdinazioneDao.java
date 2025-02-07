package com.biteme.app.persistence.txt;

import com.biteme.app.model.Ordinazione;
import com.biteme.app.model.StatoOrdine;
import com.biteme.app.model.TipoOrdine;
import com.biteme.app.persistence.OrdinazioneDao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TxtOrdinazioneDao implements OrdinazioneDao {
    private static final Logger LOGGER = Logger.getLogger(TxtOrdinazioneDao.class.getName());
    private final List<Ordinazione> ordinazioni;
    private int currentId;
    private static final String FILE_PATH = "data/ordinazioni.txt";
    private static final String DELIMITER = "\\|";
    private static final String DELIMITER_OUT = "|";

    public TxtOrdinazioneDao() {
        ordinazioni = loadFromFile();
        currentId = calculateCurrentId();
    }

    private List<Ordinazione> loadFromFile() {
        List<Ordinazione> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return list;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Ordinazione o = deserialize(line);
                if (o != null) {
                    list.add(o);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento delle ordinazioni", e);
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
            for (Ordinazione o : ordinazioni) {
                bw.write(serialize(o));
                bw.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio delle ordinazioni", e);
        }
    }

    private int calculateCurrentId() {
        return ordinazioni.stream()
                .mapToInt(Ordinazione::getId)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public Optional<Ordinazione> load(Integer id) {
        return ordinazioni.stream().filter(o -> o.getId() == id).findFirst();
    }

    @Override
    public void store(Ordinazione ordinazione) {
        if (ordinazione.getId() > 0 && exists(ordinazione.getId())) {
            delete(ordinazione.getId());
        } else if (ordinazione.getId() <= 0) {
            ordinazione.setId(currentId++);
        }
        ordinazioni.add(ordinazione);
        saveToFile();
    }

    @Override
    public void delete(Integer id) {
        ordinazioni.removeIf(o -> o.getId() == id);
        saveToFile();
    }

    @Override
    public boolean exists(Integer id) {
        return ordinazioni.stream().anyMatch(o -> o.getId() == id);
    }

    @Override
    public List<Ordinazione> getAll() {
        return new ArrayList<>(ordinazioni);
    }

    @Override
    public void aggiornaStato(int id, StatoOrdine nuovoStato) {
        ordinazioni.stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .ifPresent(o -> o.setStatoOrdine(nuovoStato));
        saveToFile();
    }

    private String serialize(Ordinazione o) {
        return o.getId() + DELIMITER_OUT +
                o.getNomeCliente() + DELIMITER_OUT +
                (o.getNumeroClienti() == null ? "" : o.getNumeroClienti()) + DELIMITER_OUT +
                o.getTipoOrdine().name() + DELIMITER_OUT +
                (o.getInfoTavolo() == null ? "" : o.getInfoTavolo()) + DELIMITER_OUT +
                o.getStatoOrdine().name() + DELIMITER_OUT +
                (o.getOrarioCreazione() == null ? "" : o.getOrarioCreazione());
    }

    private Ordinazione deserialize(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length != 7) {
            return null;
        }
        try {
            int id = Integer.parseInt(parts[0]);
            String nomeCliente = parts[1];
            String numeroClienti = parts[2];
            TipoOrdine tipoOrdine = TipoOrdine.valueOf(parts[3].toUpperCase());
            String infoTavolo = parts[4];
            StatoOrdine statoOrdine = StatoOrdine.valueOf(parts[5].toUpperCase());
            String orarioCreazione = parts[6];
            return new Ordinazione(id, nomeCliente, numeroClienti, tipoOrdine, infoTavolo, statoOrdine, orarioCreazione);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Errore nella deserializzazione della riga: {0}", line);
            return null;
        }
    }
}