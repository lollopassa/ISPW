package com.biteme.app.persistence;
import java.time.LocalDate;
import java.time.LocalTime;
import com.biteme.app.entity.Prenotazione;

import java.util.List;

public interface PrenotazioneDao extends Dao<Integer, Prenotazione> {
    List<Prenotazione> getByOrario(LocalTime orario); // Manteniamo per compatibilità
    List<Prenotazione> getByData(LocalDate data); // Nuovo metodo per filtrare con la data
    void update(Prenotazione prenotazione);
}