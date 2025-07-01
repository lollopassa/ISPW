package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.Ordine;
import com.biteme.app.persistence.OrdineDao;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryOrdineDao implements OrdineDao {

    private final List<Ordine> ordini = Storage.getInstance().getOrdini();
    private final AtomicInteger idGen;

    public InMemoryOrdineDao() {
        idGen = new AtomicInteger(
                ordini.stream().mapToInt(Ordine::getId).max().orElse(0) + 1);
    }

    @Override
    public int create(Ordine o) {
        int id = (o.getId() > 0) ? o.getId() : idGen.getAndIncrement();
        ordini.removeIf(ord -> ord.getId() == id);
        ordini.add(new Ordine(id,
                new ArrayList<>(o.getProdotti()),
                new ArrayList<>(o.getQuantita()),
                new ArrayList<>(o.getPrezzi())));
        return id;
    }

    @Override public Optional<Ordine> read(int id) {
        return ordini.stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .map(o -> new Ordine(o.getId(),
                        new ArrayList<>(o.getProdotti()),
                        new ArrayList<>(o.getQuantita()),
                        new ArrayList<>(o.getPrezzi())));
    }

    @Override public void delete(int id)        { ordini.removeIf(o -> o.getId() == id); }
    @Override public List<Ordine> getAll()      { return List.copyOf(ordini); }
}
