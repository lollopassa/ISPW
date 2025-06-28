package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.Prenotazione;
import com.biteme.app.persistence.PrenotazioneDao;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InMemoryPrenotazioneDao implements PrenotazioneDao {

    private final List<Prenotazione> prenotazioni = Storage.getInstance().getPrenotazioni();
    private int currentId = 1;

    @Override
    public Optional<Prenotazione> read(Integer key) {
                return prenotazioni.stream().filter(p -> p.getId() == key).findFirst();
    }

    @Override
    public void create(Prenotazione prenotazione) {
                if (prenotazione.getId() > 0) {
            delete(prenotazione.getId());
        } else {
                        prenotazione.setId(currentId++);
        }
        prenotazioni.add(prenotazione);
    }

    @Override
    public void delete(Integer key) {
                prenotazioni.removeIf(p -> p.getId() == key);
    }

    @Override
    public boolean exists(Integer key) {
                return prenotazioni.stream().anyMatch(p -> p.getId() == key);
    }

    @Override
    public List<Prenotazione> getByData(LocalDate data) {
                return prenotazioni.stream()
                .filter(p -> p.getData().equals(data))
                .toList();     }

    @Override
    public void update(Prenotazione prenotazione) {
                delete(prenotazione.getId());
                prenotazioni.add(prenotazione);
    }
}