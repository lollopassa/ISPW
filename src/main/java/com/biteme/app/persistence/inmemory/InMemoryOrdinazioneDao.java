package com.biteme.app.persistence.inmemory;

import com.biteme.app.entity.Ordinazione;
import com.biteme.app.entity.Ordine;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.entity.StatoOrdine;
import java.util.List;
import java.util.Optional;


public class InMemoryOrdinazioneDao implements OrdinazioneDao {
    private final List<Ordine> ordini = Storage.getInstance().getOrdini(); // Lista ordini
    private int currentId = 1; // ID univoco per le ordinazioni
    private final List<Ordinazione> ordinazioni = Storage.getInstance().getOrdinazioni(); // Usa lo storage condiviso

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
        // Prima, rimuovi tutti gli ordini collegati a questa ordinazione
        ordini.removeIf(o -> o.getId() == key);

        // Poi rimuovi l'ordinazione stessa
        boolean removed = ordinazioni.removeIf(o -> o.getId() == key);

        if (removed) {
            System.out.println("Ordinazione con ID: " + key + " eliminata con successo.");
        } else {
            System.out.println("Nessuna ordinazione trovata con ID: " + key);
        }
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

    @Override
    public void aggiornaStato(int id, StatoOrdine nuovoStato) {
        // Trova l'ordinazione con l'ID specificato
        ordinazioni.stream()
                .filter(o -> o.getId() == id) // Cerca un'ordinazione corrispondente all'ID
                .findFirst() // Ritorna la prima corrispondenza (se esiste)
                .ifPresentOrElse(
                        ordinazione -> ordinazione.setStatoOrdine(nuovoStato), // Aggiorna lo stato
                        () -> {
                            throw new IllegalArgumentException("Ordinazione con ID " + id + " non trovata.");
                        }
                );
    }
}