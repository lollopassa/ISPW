package com.biteme.app.persistence.txt;

import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.entities.TipoOrdinazione;
import com.biteme.app.persistence.OrdinazioneDao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TxtOrdinazioneDao implements OrdinazioneDao {

    private static final Logger LOGGER = Logger.getLogger(TxtOrdinazioneDao.class.getName());
    private static final String FILE_PATH     = "data/ordinazioni.txt";
    private static final String DELIMITER_IN  = "\\|";
    private static final String DELIMITER_OUT = "|";

    private final List<Ordinazione> ordinazioni;
    private final AtomicInteger idGenerator;

    public TxtOrdinazioneDao() {
        this.ordinazioni = loadFromFile();
        this.idGenerator = new AtomicInteger(
                ordinazioni.stream().mapToInt(Ordinazione::getId).max().orElse(0) + 1);
    }

    /* -------- CREATE -------- */
    @Override
    public int create(Ordinazione o) {
        int id = (o.getId() > 0) ? o.getId() : idGenerator.getAndIncrement();

        // upsert
        ordinazioni.removeIf(ord -> ord.getId() == id);
        Ordinazione copia = new Ordinazione(
                id,
                o.getNomeCliente(), o.getNumeroClienti(),
                o.getTipoOrdine(), o.getInfoTavolo(),
                o.getStatoOrdine(), o.getOrarioCreazione());
        ordinazioni.add(copia);
        saveToFile();
        return id;
    }

    /* -------- READ -------- */
    @Override
    public Optional<Ordinazione> read(int id) {
        return ordinazioni.stream().filter(o -> o.getId() == id).findFirst();
    }

    /* -------- DELETE -------- */
    @Override
    public void delete(int id) {
        ordinazioni.removeIf(o -> o.getId() == id);
        saveToFile();
    }

    /* -------- UPDATE STATO -------- */
    @Override
    public void aggiornaStato(int id, StatoOrdinazione nuovo) {
        read(id).ifPresent(o -> o.setStatoOrdine(nuovo));
        saveToFile();
    }

    /* -------- LISTA -------- */
    @Override
    public List<Ordinazione> getAll() {
        return new ArrayList<>(ordinazioni);   // copia difensiva
    }

    /* -------- I/O helper -------- */
    private void saveToFile() {
        try {
            Files.createDirectories(Paths.get("data"));
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
                for (Ordinazione o : ordinazioni) {
                    bw.write(serialize(o));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio delle ordinazioni", e);
        }
    }

    private List<Ordinazione> loadFromFile() {
        List<Ordinazione> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Optional.ofNullable(deserialize(line)).ifPresent(list::add);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento delle ordinazioni", e);
        }
        return list;
    }

    private String serialize(Ordinazione o) {
        return o.getId() + DELIMITER_OUT +
                o.getNomeCliente() + DELIMITER_OUT +
                o.getNumeroClienti() + DELIMITER_OUT +
                o.getTipoOrdine().name() + DELIMITER_OUT +
                (o.getInfoTavolo() == null ? "" : o.getInfoTavolo()) + DELIMITER_OUT +
                o.getStatoOrdine().name() + DELIMITER_OUT +
                o.getOrarioCreazione();
    }

    private Ordinazione deserialize(String line) {
        String[] p = line.split(DELIMITER_IN);
        if (p.length != 7) return null;
        try {
            return new Ordinazione(
                    Integer.parseInt(p[0]),
                    p[1],
                    p[2],
                    TipoOrdinazione.valueOf(p[3].toUpperCase()),
                    p[4].isBlank() ? null : p[4],
                    StatoOrdinazione.valueOf(p[5].toUpperCase()),
                    p[6]
            );
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Riga non valida: {0}", line);
            return null;
        }
    }
}
