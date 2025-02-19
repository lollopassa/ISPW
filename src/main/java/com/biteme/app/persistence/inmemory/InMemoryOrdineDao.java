package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.Ordine;
import com.biteme.app.persistence.OrdineDao;

import java.util.List;
import java.util.Optional;

public class InMemoryOrdineDao implements OrdineDao {

    private final List<Ordine> ordini = Storage.getInstance().getOrdini();
    private int currentId = 1;

    @Override
    public Optional<Ordine> load(Integer key) {
                return ordini.stream()
                .filter(o -> o.getId() == key)
                .findFirst();
    }

    @Override
    public void store(Ordine ordine) {
        if (ordine.getId() > 0) {
                        delete(ordine.getId());         } else {
                        ordine.setId(currentId++);
        }
                ordini.add(ordine);
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
                return ordini.stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ordine con ID " + id + " non trovato"));
    }
}