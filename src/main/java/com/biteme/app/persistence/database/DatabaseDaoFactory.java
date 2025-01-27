package com.biteme.app.persistence.database;

import com.biteme.app.persistence.DaoFactory;

/**
 * Concrete DAO factory per l'uso con il database.
 */
public class DatabaseDaoFactory extends DaoFactory {

    /**
     * Metodo per ottenere il DAO degli utenti.
     * Ritorna un'istanza del DAO specifico del database per gli utenti.
     */
    @Override
    public DatabaseUserDao getUserDao() {
        return new DatabaseUserDao();
    }

    @Override
    public DatabasePrenotazioneDao getPrenotazioneDao() {
        return new DatabasePrenotazioneDao();
    }
}