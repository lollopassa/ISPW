package com.biteme.app.persistence;

import com.biteme.app.model.Prodotto;
import java.util.List;

public interface ProdottoDao extends Dao<Integer, Prodotto> {
    List<Prodotto> getByCategoria(String categoria);
    List<Prodotto> getByDisponibilita(boolean disponibilita);
    List<Prodotto> getAll(); // Nuovo metodo per ottenere tutti i prodotti
    void update(Prodotto prodotto);
    Prodotto findByNome(String nome);
}