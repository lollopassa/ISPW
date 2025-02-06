package com.biteme.app.persistence.inmemory;

import com.biteme.app.model.Archivio;
import com.biteme.app.persistence.ArchivioDao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryArchivioDao implements ArchivioDao {

    private final List<Archivio> archivi = Storage.getInstance().getArchivi();
    private int currentId = 1;

    @Override
    public Optional<Archivio> load(Integer id) {
        return archivi.stream()
                .filter(a -> a.getIdOrdine() == id)
                .findFirst();
    }

    @Override
    public void store(Archivio archivio) {
        if (archivio.getIdOrdine() == 0) {
            archivio.setIdOrdine(currentId++);
        } else {
            delete(archivio.getIdOrdine());
        }
        archivi.add(archivio);
    }

    @Override
    public void delete(Integer id) {
        archivi.removeIf(a -> a.getIdOrdine() == id);
    }

    @Override
    public boolean exists(Integer id) {
        return archivi.stream().anyMatch(a -> a.getIdOrdine() == id);
    }

    @Override
    public List<Archivio> getAll() {
        return new ArrayList<>(archivi);
    }

    @Override
    public List<Archivio> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Archivio> filteredArchivi = new ArrayList<>();
        for (Archivio archivio : archivi) {
            if (!archivio.getDataArchiviazione().isBefore(startDate) &&
                    !archivio.getDataArchiviazione().isAfter(endDate)) {
                filteredArchivi.add(archivio);
            }
        }
        return filteredArchivi;
    }
}