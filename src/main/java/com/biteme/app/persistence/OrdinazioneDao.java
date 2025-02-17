package com.biteme.app.persistence;

import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;

import java.util.List;

public interface OrdinazioneDao extends Dao<Integer, Ordinazione> {

    List<Ordinazione> getAll();
    void aggiornaStato(int id, StatoOrdinazione nuovoStato); // Usa direttamente l'enum StatoOrdine
}