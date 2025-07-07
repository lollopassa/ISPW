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
        return storage.stream()
                .filter(a -> a.getIdOrdine().equals(id))
                .findFirst();
    }

    @Override
    public void create(Archivio a) {
        storage.removeIf(x -> x.getIdOrdine().equals(a.getIdOrdine()));

        var righeValide = a.getRighe().stream()
                .filter(r -> r.getProdotto().getId() > 0)
                .toList();

        storage.add(new Archivio(
                a.getIdOrdine(),
                righeValide,
                a.getTotale(),
                a.getDataArchiviazione()
        ));
    }

    @Override
    public void delete(Integer id) {
        storage.removeIf(a -> a.getIdOrdine().equals(id));
    }

    @Override
    public boolean exists(Integer id) {
        return storage.stream().anyMatch(a -> a.getIdOrdine().equals(id));
    }

    @Override
    public List<Archivio> getAll() {
        return List.copyOf(storage);
    }

    @Override
    public List<Archivio> findByDateRange(LocalDateTime s, LocalDateTime e) {
        var out = new ArrayList<Archivio>();
        for (var a : storage) {
            var d = a.getDataArchiviazione();
            if (!d.isBefore(s) && !d.isAfter(e)) {
                out.add(a);
            }
        }
        return out;
    }
}
