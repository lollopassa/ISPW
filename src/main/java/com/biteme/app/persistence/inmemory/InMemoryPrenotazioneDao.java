package com.biteme.app.persistence.inmemory;

import com.biteme.app.model.Prenotazione;
import com.biteme.app.persistence.PrenotazioneDao;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class InMemoryPrenotazioneDao implements PrenotazioneDao {

    private final List<Prenotazione> prenotazioni = Storage.getInstance().getPrenotazioni(); // Usa lo storage condiviso
    private int currentId = 1;

    @Override
    public Optional<Prenotazione> load(Integer key) {
        // Ritorna una prenotazione dato un ID
        return prenotazioni.stream().filter(p -> p.getId() == key).findFirst();
    }

    @Override
    public void store(Prenotazione prenotazione) {
        // Se l'entità ha già un ID, effettua un aggiornamento.
        if (prenotazione.getId() > 0) {
            delete(prenotazione.getId());
        } else {
            // Altrimenti assegna un ID univoco.
            prenotazione.setId(currentId++);
        }
        prenotazioni.add(prenotazione);
    }

    @Override
    public void delete(Integer key) {
        // Rimuove la prenotazione con un ID specifico
        prenotazioni.removeIf(p -> p.getId() == key);
    }

    @Override
    public boolean exists(Integer key) {
        // Verifica se esiste una prenotazione con un ID specifico
        return prenotazioni.stream().anyMatch(p -> p.getId() == key);
    }


    @Override
    public List<Prenotazione> getByOrario(LocalTime orario) {
        // Filtra le prenotazioni dall'orario
        return prenotazioni.stream()
                .filter(p -> p.getOrario().equals(orario))
                .toList(); // Sostituito "collect(Collectors.toList())" con "toList()"
    }

    @Override
    public List<Prenotazione> getByData(LocalDate data) {
        // Filtra le prenotazioni dalla data
        return prenotazioni.stream()
                .filter(p -> p.getData().equals(data))
                .toList(); // Sostituito "collect(Collectors.toList())" con "toList()"
    }

    @Override
    public void update(Prenotazione prenotazione) {
        // Trova la prenotazione con l'ID specificato e rimuovila
        delete(prenotazione.getId());
        // Aggiungi la versione aggiornata
        prenotazioni.add(prenotazione);
    }
}