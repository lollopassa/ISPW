package com.biteme.app.persistence;

import com.biteme.app.model.Ordinazione;
import com.biteme.app.model.StatoOrdine;

import java.util.List;

public interface OrdinazioneDao extends Dao<Integer, Ordinazione> {

    List<Ordinazione> getAll();
    void aggiornaStato(int id, StatoOrdine nuovoStato); // Usa direttamente l'enum StatoOrdine
}