package com.biteme.app.persistence.txt;

import com.biteme.app.entities.Ordine;
import com.biteme.app.persistence.OrdineDao;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TxtOrdineDao implements OrdineDao {

    private static final Logger LOG = Logger.getLogger(TxtOrdineDao.class.getName());

    /* file + costanti di serializzazione */
    private static final Path   FILE     = Path.of("data", "ordini.txt");
    private static final String SEP      = "\\|";
    private static final String SEP_OUT  = "|";
    private static final String EMPTY    = "EMPTY";

    private final List<Ordine>      ordini;
    private final AtomicInteger     idGen;

    /* ---------- costruttore ---------- */
    public TxtOrdineDao() {
        this.ordini = load();
        this.idGen  = new AtomicInteger(
                ordini.stream().mapToInt(Ordine::getId).max().orElse(0) + 1);
    }

    /* ---------- CRUD ---------- */
    @Override
    public int create(Ordine o) {
        int id = (o.getId() > 0) ? o.getId() : idGen.getAndIncrement();
        ordini.removeIf(ord -> ord.getId() == id);
        ordini.add(new Ordine(id, o.getProdotti(), o.getQuantita(), o.getPrezzi()));
        save();
        return id;
    }

    @Override
    public Optional<Ordine> read(int id) {
        return ordini.stream().filter(o -> o.getId() == id).findFirst();
    }

    @Override
    public void delete(int id) {
        ordini.removeIf(o -> o.getId() == id);
        save();
    }

    @Override
    public List<Ordine> getAll() {
        return new ArrayList<>(ordini);    // copia difensiva
    }

    /* ---------- I/O ---------- */
    private List<Ordine> load() {
        List<Ordine> list = new ArrayList<>();
        try {
            Files.createDirectories(FILE.getParent());
            if (!Files.exists(FILE)) return list;

            try (BufferedReader br = Files.newBufferedReader(FILE)) {
                String line;
                while ((line = br.readLine()) != null) {
                    deserialize(line).ifPresent(list::add);
                }
            }
        } catch (IOException e) {
            LOG.warning(e::getMessage);
        }
        return list;
    }

    private void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(FILE)) {
            for (Ordine o : ordini) {
                bw.write(serialize(o));
                bw.newLine();
            }
        } catch (IOException e) {
            LOG.warning(e::getMessage);
        }
    }

    /* ---------- serializzazione ---------- */
    private String serialize(Ordine o) {
        String prodStr = o.getProdotti().isEmpty()
                ? EMPTY
                : String.join(",", o.getProdotti());

        String qtyStr = o.getQuantita().isEmpty()
                ? EMPTY
                : o.getQuantita().toString().replaceAll("[\\[\\] ]", "");

        String priceStr = (o.getPrezzi() == null || o.getPrezzi().isEmpty())
                ? EMPTY
                : o.getPrezzi().stream()
                .map(BigDecimal::toString)
                .collect(Collectors.joining(","));

        return o.getId() + SEP_OUT + prodStr + SEP_OUT + qtyStr + SEP_OUT + priceStr;
    }

    private Optional<Ordine> deserialize(String ln) {
        String[] p = ln.split(SEP);
        if (p.length != 4) return Optional.empty();

        try {
            int id = Integer.parseInt(p[0]);

            List<String> prod = p[1].equals(EMPTY)
                    ? List.of()
                    : Arrays.asList(p[1].split(","));

            List<Integer> qty = p[2].equals(EMPTY)
                    ? List.of()
                    : Arrays.stream(p[2].split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();                     // <-- nuovo

            List<BigDecimal> pr = p[3].equals(EMPTY)
                    ? List.of()
                    : Arrays.stream(p[3].split(","))
                    .map(String::trim)
                    .map(BigDecimal::new)
                    .toList();                     // <-- nuovo

            return Optional.of(new Ordine(id, prod, qty, pr));

        } catch (Exception e) {
            LOG.warning(() -> "Riga non valida: " + ln);
            return Optional.empty();
        }
    }

}
