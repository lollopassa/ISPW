package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.Ordine;
import com.biteme.app.persistence.OrdineDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryOrdineDao implements OrdineDao {
    private final List<Ordine> ordini = new ArrayList<>();
    private int currentId = 1;

    @Override
    public Optional<Ordine> read(Integer key) {
        return ordini.stream()
                .filter(o -> o.getId() == key)
                .findFirst()
                .map(o -> new Ordine(
                        o.getId(),
                        new ArrayList<>(o.getProdotti()),
                        new ArrayList<>(o.getQuantita()),
                        new ArrayList<>(o.getPrezzi())
                ));
    }

    @Override
    public void create(Ordine ordine) {
        if (ordine.getId() > 0) {
            delete(ordine.getId());
        } else {
            ordine.setId(currentId++);
        }
        ordini.add(new Ordine(
                ordine.getId(),
                new ArrayList<>(ordine.getProdotti()),
                new ArrayList<>(ordine.getQuantita()),
                new ArrayList<>(ordine.getPrezzi())
        ));
    }

    @Override
    public void delete(Integer key) {
        ordini.removeIf(o -> o.getId() == key);
    }

    @Override
    public boolean exists(Integer key) {
        return ordini.stream().anyMatch(o -> o.getId() == key);
    }

    @Override
    public Ordine getById(Integer id) {
        return read(id).orElseThrow(() -> new IllegalArgumentException("Ordine con ID " + id + " non trovato"));
    }
}
