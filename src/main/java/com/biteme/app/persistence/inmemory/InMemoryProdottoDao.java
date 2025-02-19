package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.Prodotto;
import com.biteme.app.persistence.ProdottoDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class InMemoryProdottoDao implements ProdottoDao {

    private final List<Prodotto> prodotti = Storage.getInstance().getProdotti();
    private int currentId = 1;

    @Override
    public Optional<Prodotto> load(Integer key) {
                return prodotti.stream()
                .filter(p -> p.getId() == key)
                .findFirst();
    }

    @Override
    public void store(Prodotto prodotto) {
                if (prodotto.getId() > 0) {
            delete(prodotto.getId());
        } else {
                        prodotto.setId(currentId++);
        }
        prodotti.add(prodotto);
    }

    @Override
    public void delete(Integer key) {
                prodotti.removeIf(p -> p.getId() == key);
    }

    @Override
    public boolean exists(Integer key) {
                return prodotti.stream()
                .anyMatch(p -> p.getId() == key);
    }

    @Override
    public List<Prodotto> getByCategoria(String categoria) {
                return prodotti.stream()
                .filter(p -> p.getCategoria().name().equalsIgnoreCase(categoria))
                .toList();
    }

    @Override
    public List<Prodotto> getByDisponibilita(boolean disponibilita) {
                return prodotti.stream()
                .filter(p -> p.isDisponibile() == disponibilita)
                .toList();
    }

    @Override
    public List<Prodotto> getAll() {
        return new ArrayList<>(prodotti);     }

    @Override
    public Prodotto findByNome(String nome) {
        return Storage.getInstance().getProdotti().stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
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