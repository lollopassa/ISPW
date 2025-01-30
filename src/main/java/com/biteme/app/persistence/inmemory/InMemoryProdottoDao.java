package com.biteme.app.persistence.inmemory;

import com.biteme.app.entity.Prodotto;
import com.biteme.app.persistence.ProdottoDao;

import java.util.List;
import java.util.Optional;


public class InMemoryProdottoDao implements ProdottoDao {

    private final List<Prodotto> prodotti = Storage.getInstance().getProdotti(); // Usa lo storage condiviso
    private int currentId = 1;

    @Override
    public Optional<Prodotto> load(Integer key) {
        // Ritorna un prodotto dato un ID
        return prodotti.stream()
                .filter(p -> p.getId() == key)
                .findFirst();
    }

    @Override
    public void store(Prodotto prodotto) {
        // Se l'entità ha già un ID, effettua un aggiornamento.
        if (prodotto.getId() > 0) {
            delete(prodotto.getId());
        } else {
            // Altrimenti assegna un ID univoco.
            prodotto.setId(currentId++);
        }
        prodotti.add(prodotto);
    }

    @Override
    public void delete(Integer key) {
        // Rimuove il prodotto con un ID specifico
        prodotti.removeIf(p -> p.getId() == key);
    }

    @Override
    public boolean exists(Integer key) {
        // Verifica se esiste un prodotto con un ID specifico
        return prodotti.stream()
                .anyMatch(p -> p.getId() == key);
    }

    @Override
    public List<Prodotto> getByCategoria(String categoria) {
        // Filtra i prodotti in base alla categoria
        return prodotti.stream()
                .filter(p -> p.getCategoria().name().equalsIgnoreCase(categoria))
                .toList();
    }

    @Override
    public List<Prodotto> getByDisponibilita(boolean disponibilita) {
        // Filtra i prodotti per disponibilità
        return prodotti.stream()
                .filter(p -> p.isDisponibile() == disponibilita)
                .toList();
    }

    @Override
    public void update(Prodotto prodotto) {
        prodotti.stream()
                .filter(p -> p.getId() == prodotto.getId())
                .findFirst()
                .ifPresent(existingProdotto -> {
                    existingProdotto.setNome(prodotto.getNome());
                    existingProdotto.setCategoria(prodotto.getCategoria());
                    existingProdotto.setPrezzo(prodotto.getPrezzo());
                    existingProdotto.setDisponibile(prodotto.isDisponibile());
                });
    }
}