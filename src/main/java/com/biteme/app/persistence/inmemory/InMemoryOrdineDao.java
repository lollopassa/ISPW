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
        // Se l'ordine ha già un ID, effettua un aggiornamento
        if (ordine.getId() > 0) {
            delete(ordine.getId());
        } else {
            // Altrimenti assegna un nuovo ID unico
            ordine.setId(currentId++);
        }
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
}
