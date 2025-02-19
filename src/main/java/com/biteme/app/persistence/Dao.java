package com.biteme.app.persistence;

import java.util.Optional;

public interface Dao<K, V> {

        Optional<V> load(K key);

        void store(V entity);

        void delete(K key);

        boolean exists(K key);
}