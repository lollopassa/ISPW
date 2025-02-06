package com.biteme.app.persistence;

import com.biteme.app.model.Archivio;

import java.time.LocalDateTime;
import java.util.List;

public interface ArchivioDao extends Dao<Integer, Archivio> {
    List<Archivio> getAll();
    List<Archivio> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}