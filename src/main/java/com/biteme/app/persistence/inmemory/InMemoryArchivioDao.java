package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.Archivio;
import com.biteme.app.persistence.ArchivioDao;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryArchivioDao implements ArchivioDao {

    private final List<Archivio> storage = new CopyOnWriteArrayList<>();

    @Override
    public Optional<Archivio> read(Integer id) {
        return storage.stream().filter(a -> a.getIdOrdine() == id).findFirst();
    }

    @Override
    public void create(Archivio a) {
        // elimina eventuale esistente con stesso id
        storage.removeIf(x -> x.getIdOrdine() == a.getIdOrdine());
        storage.add(a);
    }

    @Override
    public void delete(Integer id) {
        storage.removeIf(a -> a.getIdOrdine() == id);
    }

    @Override
    public boolean exists(Integer id) {
        return storage.stream().anyMatch(a -> a.getIdOrdine() == id);
    }

    @Override
    public List<Archivio> getAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public List<Archivio> findByDateRange(LocalDateTime s, LocalDateTime e) {
        List<Archivio> out = new ArrayList<>();
        for (Archivio a : storage) {
            LocalDateTime d = a.getDataArchiviazione();
            if (!d.isBefore(s) && !d.isAfter(e)) out.add(a);
        }
        return out;
    }
}
