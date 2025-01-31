package com.biteme.app.persistence.inmemory;

import com.biteme.app.entity.Ordinazione;
import com.biteme.app.persistence.OrdinazioneDao;

import java.util.List;
import java.util.Optional;


public class InMemoryOrdinazioneDao implements OrdinazioneDao {

    private final List<Ordinazione> ordinazioni = Storage.getInstance().getOrdinazioni(); // Usa lo storage condiviso
    private int currentId = 1;

    @Override
    public Optional<Ordinazione> load(Integer key) {
        // Trova un ordine tramite la chiave (ID)
        return ordinazioni.stream()
                .filter(o -> o.getId() == key) // Confronto diretto su valori `long`
                .findFirst();
    }

    @Override
    public void store(Ordinazione ordinazione) {
        // Se l'entità ha già un ID, effettua un aggiornamento
        if (ordinazione.getId() > 0) {
            delete(ordinazione.getId()); // Passa `long`, che ora è accettato
        } else {
            // Altrimenti assegna un nuovo ID unico
            ordinazione.setId(currentId++);
        }
        ordinazioni.add(ordinazione);
    }

    @Override
    public void delete(Integer key) {
        // Rimuove l'ordine tramite l'ID
        ordinazioni.removeIf(o -> o.getId() == key); // Confronto diretto su `long`
    }

    @Override
    public boolean exists(Integer key) {
        // Controlla se un ordine con l'ID specificato esiste
        return ordinazioni.stream().anyMatch(o -> o.getId() == key); // Confronto diretto su `long`
    }

    @Override
    public List<Ordinazione> getAll() {
        // Ritorna la lista di tutti gli ordini
        return ordinazioni;
    }
}