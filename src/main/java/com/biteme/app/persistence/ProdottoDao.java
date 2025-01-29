package com.biteme.app.persistence;

import com.biteme.app.entity.Prodotto;
import java.util.List;

public interface ProdottoDao extends Dao<Integer, Prodotto> {
    List<Prodotto> getByCategoria(String categoria);
    List<Prodotto> getByDisponibilita(boolean disponibilita);
}