package com.biteme.app.persistence;

import com.biteme.app.entity.Ordinazione;
import com.biteme.app.entity.StatoOrdine; // Import dell'enum StatoOrdine

import java.util.List;

public interface OrdinazioneDao extends Dao<Integer, Ordinazione> {

    List<Ordinazione> getAll();

    // Metodo per aggiornare lo stato di una ordinazione basata sull'ID
    void aggiornaStato(int id, StatoOrdine nuovoStato); // Usa direttamente l'enum StatoOrdine
}