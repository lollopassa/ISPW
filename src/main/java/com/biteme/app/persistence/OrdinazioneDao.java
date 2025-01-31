package com.biteme.app.persistence;

import com.biteme.app.entity.Ordinazione;

import java.util.List;

public interface OrdinazioneDao extends Dao<Integer, Ordinazione> {

    List<Ordinazione> getAll();
}
