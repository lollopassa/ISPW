package com.biteme.app.persistence;

import java.util.Optional;

public interface Dao<K, V> {

        void create(V entity);

        Optional<V> read(K key);

        void delete(K key);

        boolean exists(K key);
}