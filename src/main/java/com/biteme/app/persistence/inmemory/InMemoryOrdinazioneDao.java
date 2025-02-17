package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.Ordine;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.entities.StatoOrdinazione;

import java.util.List;
import java.util.Optional;

public class InMemoryOrdinazioneDao implements OrdinazioneDao {

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
    public void aggiornaStato(int id, StatoOrdinazione nuovoStato) {
        // Aggiorna lo stato solo se l'ordinazione esiste
        ordinazioni.stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .ifPresent(ordinazione -> ordinazione.setStatoOrdine(nuovoStato));
    }
}