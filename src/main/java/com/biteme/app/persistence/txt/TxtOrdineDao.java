// com/biteme/app/persistence/txt/TxtOrdineDao.java

package com.biteme.app.persistence.txt;

import com.biteme.app.entities.Ordine;
import com.biteme.app.persistence.OrdineDao;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TxtOrdineDao implements OrdineDao {

    private static final Logger LOG = Logger.getLogger(TxtOrdineDao.class.getName());
    private static final Path FILE = Path.of("data", "ordini.txt");
    private static final String SEP = "\\|";
    private static final String SEP_OUT = "|";
    private static final String EMPTY = "EMPTY";

    private final List<Ordine> ordini;
    private final AtomicInteger idGen;

    public TxtOrdineDao() {
        this.ordini = load();
        this.idGen = new AtomicInteger(
                ordini.stream().mapToInt(Ordine::getId).max().orElse(0) + 1
        );
    }

    @Override
    public int create(Ordine o) {
        int id = (o.getId() > 0) ? o.getId() : idGen.getAndIncrement();
        ordini.removeIf(x -> x.getId() == id);

        Ordine copy = new Ordine(
                id,
                new ArrayList<>(o.getProdotti()),
                new ArrayList<>(o.getQuantita()),
                new ArrayList<>(o.getPrezzi())
        );
        ordini.add(copy);
        save();
        return id;
    }

    @Override
    public Optional<Ordine> read(int id) {
        return ordini.stream().filter(x -> x.getId() == id).findFirst();
    }

    @Override
    public void delete(int id) {
        ordini.removeIf(x -> x.getId() == id);
        save();
    }

    @Override
    public List<Ordine> getAll() {
        return new ArrayList<>(ordini);
    }

    private List<Ordine> load() {
        List<Ordine> list = new ArrayList<>();
        try {
            Files.createDirectories(FILE.getParent());
            if (!Files.exists(FILE)) return list;
            try (BufferedReader br = Files.newBufferedReader(FILE)) {
                String l;
                while ((l = br.readLine()) != null) {
                    deserialize(l).ifPresent(list::add);
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

    private String serialize(Ordine o) {
        String prod = o.getProdotti().isEmpty()
                ? EMPTY
                : String.join(",", o.getProdotti());
        String qty = o.getQuantita().isEmpty()
                ? EMPTY
                : o.getQuantita().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String pr = o.getPrezzi().isEmpty()
                ? EMPTY
                : o.getPrezzi().stream()
                .map(BigDecimal::toPlainString)
                .collect(Collectors.joining(","));
        return o.getId() + SEP_OUT + prod + SEP_OUT + qty + SEP_OUT + pr;
    }

    private Optional<Ordine> deserialize(String ln) {
        String[] p = ln.split(SEP, -1);
        if (p.length != 4) return Optional.empty();
        try {
            int id = Integer.parseInt(p[0]);
            List<String> prod = p[1].equals(EMPTY)
                    ? List.of()
                    : Arrays.stream(p[1].split(",")).toList();
            List<Integer> qty = p[2].equals(EMPTY)
                    ? List.of()
                    : Arrays.stream(p[2].split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();
            List<BigDecimal> pr = p[3].equals(EMPTY)
                    ? List.of()
                    : Arrays.stream(p[3].split(","))
                    .map(String::trim)
                    .map(BigDecimal::new)
                    .toList();
            return Optional.of(new Ordine(id, prod, qty, pr));
        } catch (Exception ex) {
            LOG.warning("Riga non valida: " + ln);
            return Optional.empty();
        }
    }
}
