package com.biteme.app.persistence;

import com.biteme.app.entities.Ordine;

public interface OrdineDao extends Dao<Integer, Ordine> {
    Ordine getById(Integer id); // Metodo per ottenere un ordine tramite ID
}