package com.biteme.app.persistence.txt;

import com.biteme.app.entities.Categoria;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.persistence.ProdottoDao;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TxtProdottoDao implements ProdottoDao {
    private static final Logger LOGGER = Logger.getLogger(TxtProdottoDao.class.getName());
    private final List<Prodotto> prodotti;
    private int currentId;
    private static final String FILE_PATH = "data/prodotti.txt";
    private static final String DELIMITER = "\\|";
    private static final String DELIMITER_OUT = "|";

    public TxtProdottoDao() {
        prodotti = loadFromFile();
        currentId = calculateCurrentId();
    }

    private List<Prodotto> loadFromFile() {
        List<Prodotto> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Prodotto p = deserialize(line);
                if (p != null) list.add(p);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento dei prodotti", e);
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
            for (Prodotto p : prodotti) {
                bw.write(serialize(p));
                bw.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio dei prodotti", e);
        }
    }

    private int calculateCurrentId() {
        return prodotti.stream()
                .mapToInt(Prodotto::getId)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public Optional<Prodotto> read(Integer id) {
        return prodotti.stream().filter(p -> p.getId() == id).findFirst();
    }

    @Override
    public void create(Prodotto prodotto) {
        if (prodotto.getId() > 0) {
            delete(prodotto.getId());
        } else {
            prodotto.setId(currentId++);
        }
        prodotti.add(prodotto);
        saveToFile();
    }

    @Override
    public List<Prodotto> getByDisponibilita(boolean disponibilita) {
        return prodotti.stream()
                .filter(p -> p.isDisponibile() == disponibilita)
                .toList();
    }

    @Override
    public void delete(Integer id) {
        prodotti.removeIf(p -> p.getId() == id);
        saveToFile();
    }

    @Override
    public boolean exists(Integer id) {
        return prodotti.stream().anyMatch(p -> p.getId() == id);
    }

    @Override
    public Prodotto findByNome(String nome) {
        return prodotti.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst().orElse(null);
    }

    @Override
    public void update(Prodotto prodotto) {
        for (int i = 0; i < prodotti.size(); i++) {
            if (prodotti.get(i).getId() == prodotto.getId()) {
                prodotti.set(i, prodotto);
                break;
            }
        }
        saveToFile();
    }

    @Override
    public List<Prodotto> getAll() {
        return new ArrayList<>(prodotti);
    }


    private String serialize(Prodotto p) {
        return p.getId() + DELIMITER_OUT +
                p.getNome() + DELIMITER_OUT +
                p.getPrezzo().toPlainString() + DELIMITER_OUT +
                p.getCategoria().name() + DELIMITER_OUT +
                p.isDisponibile();
    }

    @Override
    public List<Prodotto> getByCategoria(String categoria) {
        return prodotti.stream()
                .filter(p -> p.getCategoria().name().equalsIgnoreCase(categoria))
                .toList();
    }

    private Prodotto deserialize(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length != 5) {
            return null;
        }
        try {
            int id = Integer.parseInt(parts[0]);
            String nome = parts[1];
            BigDecimal prezzo = new BigDecimal(parts[2]);
            Categoria categoria = Categoria.valueOf(parts[3].toUpperCase());
            boolean disponibile = Boolean.parseBoolean(parts[4]);
            return new Prodotto(id, nome, prezzo, categoria, disponibile);
        } catch (Exception _) {
            LOGGER.log(Level.WARNING, "Errore nella deserializzazione della riga: {0}", line);
            return null;
        }
    }
}