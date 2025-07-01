package com.biteme.app.persistence;

import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;

import java.util.List;
import java.util.Optional;

/** DAO per le ordinazioni: salva, legge, aggiorna lo stato. */
public interface OrdinazioneDao {

    /** Crea l’ordinazione e restituisce l’ID generato. */
    int create(Ordinazione ordinazione);

    /** Lettura singola (opzionale nel controller, ma utile in generale). */
    Optional<Ordinazione> read(int id);

    /** Aggiornamento puntuale dello stato. */
    void aggiornaStato(int id, StatoOrdinazione nuovoStato);

    /** Eliminazione. */
    void delete(int id);

    /** Elenco completo. */
    List<Ordinazione> getAll();

    /** Helper di default. */
    default boolean exists(int id) { return read(id).isPresent(); }
}
