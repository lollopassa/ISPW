package com.biteme.app.persistence;

import com.biteme.app.entities.Ordine;

import java.util.List;
import java.util.Optional;

public interface OrdineDao {

    /** Inserisce (o sovrascrive) l’ordine e restituisce l’ID persistito. */
    int create(Ordine ordine);

    /** Lettura singola. */
    Optional<Ordine> read(int id);

    /** Eliminazione. */
    void delete(int id);


    /** Elenco completo (facoltativo, non usato nel controller). */
    List<Ordine> getAll();

    /** Helper default. */
    default boolean exists(int id) { return read(id).isPresent(); }

    /** Throw-oriented convenience. */
    default Ordine getById(int id) {
        return read(id).orElseThrow(
                () -> new IllegalArgumentException("Ordine con ID " + id + " non trovato"));
    }
}
