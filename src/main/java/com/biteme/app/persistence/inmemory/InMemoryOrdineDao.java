package com.biteme.app.persistence.inmemory;

import com.biteme.app.entity.Ordine;
import com.biteme.app.persistence.OrdineDao;

import java.util.List;
import java.util.Optional;

public class InMemoryOrdineDao implements OrdineDao {

    private final List<Ordine> ordini = Storage.getInstance().getOrdini(); // Usa lo storage condiviso
    private int currentId = 1;

    @Override
    public Optional<Ordine> load(Integer key) {
        // Trova un ordine tramite l'ID
        return ordini.stream()
                .filter(o -> o.getId() == key)
                .findFirst();
    }

    @Override
    public void store(Ordine ordine) {
        if (ordine.getId() > 0) {
            // Se l'ID è già impostato, verifica che esista già un ordine con quell'ID e aggiorna
            delete(ordine.getId()); // Rimuovi un eventuale ordine con lo stesso ID
        } else {
            // Se l'ID non è impostato, genera un nuovo ID unico
            ordine.setId(currentId++);
        }
        // Aggiungi l'ordine (nuovo o aggiornato) alla lista
        ordini.add(ordine);
    }

    @Override
    public void delete(Integer key) {
        // Rimuove l'ordine tramite l'ID
        ordini.removeIf(o -> o.getId() == key);
    }

    @Override
    public boolean exists(Integer key) {
        // Controlla se esiste un ordine con l'ID specificato
        return ordini.stream().anyMatch(o -> o.getId() == key);
    }

    @Override
    public Ordine getById(Integer id) {
        // Cerca un ordine tramite il suo ID, senza restituire un Optional
        return ordini.stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ordine con ID " + id + " non trovato"));
    }
}