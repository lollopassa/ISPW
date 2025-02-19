package com.biteme.app.persistence;

import com.biteme.app.persistence.inmemory.InMemoryDaoFactory;
import com.biteme.app.persistence.database.DatabaseDaoFactory;
import com.biteme.app.persistence.txt.TxtDaoFactory;

public enum PersistenceProvider {

        IN_MEMORY("in memory", new InMemoryDaoFactory()),

        DATABASE("database", new DatabaseDaoFactory()),

        TXT("txt", new TxtDaoFactory());


    private final String name;
    private final DaoFactory daoFactory;

    PersistenceProvider(String name, DaoFactory daoFactory) {
        this.name = name;
        this.daoFactory = daoFactory;
    }

    public String getName() {
        return name;
    }

    public DaoFactory getDaoFactory() {
        return daoFactory;
    }

    public static PersistenceProvider getProviderByName(String name) {
        for (PersistenceProvider provider : values()) {
            if (provider.getName().equalsIgnoreCase(name)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Provider di persistenza non trovato per il nome: " + name);
    }

}