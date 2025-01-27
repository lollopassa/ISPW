package com.biteme.app.persistence;

import java.util.Optional;

public interface Dao<K, V> {

    // Operazione per caricare un oggetto in base alla chiave primaria (es. ID)
    Optional<V> load(K key);

    // Operazione per salvare o aggiornare un oggetto
    void store(V entity);

    // Operazione per eliminare un oggetto
    void delete(K key);

    // Operazione per verificare se un oggetto esiste
    boolean exists(K key);
}