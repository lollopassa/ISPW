// com/biteme/app/persistence/inmemory/InMemoryOrdineDao.java

package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.Ordine;
import com.biteme.app.persistence.OrdineDao;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryOrdineDao implements OrdineDao {

    private final List<Ordine> ordini = Storage.getInstance().getOrdini();
    private final AtomicInteger idGen;

    public InMemoryOrdineDao() {
        this.idGen = new AtomicInteger(
                ordini.stream().mapToInt(Ordine::getId).max().orElse(0) + 1
        );
    }

    @Override
    public int create(Ordine o) {
        int id = (o.getId() > 0) ? o.getId() : idGen.getAndIncrement();

        // rimuovo eventuale vecchio
        ordini.removeIf(x -> x.getId() == id);

        // deep‐copy delle liste
        Ordine copy = new Ordine(
                id,
                new ArrayList<>(o.getProdotti()),
                new ArrayList<>(o.getQuantita()),
                new ArrayList<>(o.getPrezzi())
        );
        ordini.add(copy);
        return id;
    }

    @Override
    public Optional<Ordine> read(int id) {
        return ordini.stream()
                .filter(x -> x.getId() == id)
                .findFirst()
                .map(x -> new Ordine(
                        x.getId(),
                        new ArrayList<>(x.getProdotti()),
                        new ArrayList<>(x.getQuantita()),
                        new ArrayList<>(x.getPrezzi())
                ));
    }

    @Override
    public void delete(int id) {
        ordini.removeIf(x -> x.getId() == id);
    }

    @Override
    public List<Ordine> getAll() {
        // copia difensiva
        List<Ordine> out = new ArrayList<>();
        for (Ordine x : ordini) {
            out.add(new Ordine(
                    x.getId(),
                    new ArrayList<>(x.getProdotti()),
                    new ArrayList<>(x.getQuantita()),
                    new ArrayList<>(x.getPrezzi())
            ));
        }
        return out;
    }
}
