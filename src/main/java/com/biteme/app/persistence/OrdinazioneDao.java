package com.biteme.app.persistence;

import com.biteme.app.entity.Ordine;

import java.util.List;

public interface OrdinazioneDao extends Dao<Integer, Ordine> {

    List<Ordine> getAll();
}
