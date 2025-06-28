package com.biteme.app.persistence;
import java.time.LocalDate;
import com.biteme.app.entities.Prenotazione;

import java.util.List;

public interface PrenotazioneDao extends Dao<Integer, Prenotazione> {
    List<Prenotazione> getByData(LocalDate data);
    void update(Prenotazione prenotazione);
}