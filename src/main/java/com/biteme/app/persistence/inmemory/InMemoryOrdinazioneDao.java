package com.biteme.app.persistence.inmemory;

import com.biteme.app.model.Ordinazione;
import com.biteme.app.model.Ordine;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.model.StatoOrdine;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class InMemoryOrdinazioneDao implements OrdinazioneDao {

    private static final Logger logger = Logger.getLogger(InMemoryOrdinazioneDao.class.getName());
    private final List<Ordine> ordini = Storage.getInstance().getOrdini(); // Lista ordini
    private int currentId = 1; // ID univoco per le ordinazioni
    private final List<Ordinazione> ordinazioni = Storage.getInstance().getOrdinazioni(); // Usa lo storage condiviso

    @Override
    public Optional<Ordinazione> load(Integer key) {
        return ordinazioni.stream()
                .filter(o -> o.getId() == key) // Confronto diretto su valori
                .findFirst();
    }

    @Override
    public void store(Ordinazione ordinazione) {
        // Effettua un aggiornamento solo se necessario
        if (ordinazione.getId() > 0 && exists(ordinazione.getId())) {
            delete(ordinazione.getId());
        } else if (ordinazione.getId() <= 0) {
            // Assegna un nuovo ID unico solo se non già presente
            ordinazione.setId(currentId++);
        }
        ordinazioni.add(ordinazione);
    }

    @Override
    public void delete(Integer key) {
        if (exists(key)) { // Elimina solo se l'ID esiste
            // Rimuovi tutti gli ordini collegati a questa ordinazione
            ordini.removeIf(o -> o.getId() == key);

            // Rimuovi l'ordinazione stessa
            ordinazioni.removeIf(o -> o.getId() == key);
        }
    }

    @Override
    public boolean exists(Integer key) {
        // Controlla se un ordine con l'ID specificato esiste
        return ordinazioni.stream().anyMatch(o -> o.getId() == key);
    }

    @Override
    public List<Ordinazione> getAll() {
        // Ritorna la lista di tutti gli ordini
        return ordinazioni;
    }

    @Override
    public void aggiornaStato(int id, StatoOrdine nuovoStato) {
        // Aggiorna lo stato solo se l'ordinazione esiste
        ordinazioni.stream()
                .filter(o -> o.getId() == id) // Cerca un'ordinazione corrispondente all'ID
                .findFirst() // Ritorna la prima corrispondenza (se esiste)
                .ifPresentOrElse(
                        ordinazione -> ordinazione.setStatoOrdine(nuovoStato), // Aggiorna lo stato
                        () -> logger.warning(String.format("Ordinazione con ID %d non trovata. Stato non aggiornato.", id))
                );
    }
}