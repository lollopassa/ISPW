package com.biteme.app.persistence;

import com.biteme.app.entity.Archivio;

import java.util.List;

public interface ArchivioDao extends Dao<Integer, Archivio> {
    List<Archivio> getAll();
}