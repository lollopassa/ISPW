package com.biteme.app.persistence.database;

import com.biteme.app.persistence.DaoFactory;


public class DatabaseDaoFactory extends DaoFactory {


    @Override
    public DatabaseUserDao getUserDao() {
        return new DatabaseUserDao();
    }

    @Override
    public DatabasePrenotazioneDao getPrenotazioneDao() {
        return new DatabasePrenotazioneDao();
    }

    @Override
    public DatabaseOrdinazioneDao getOrdinazioneDao() {
        return new DatabaseOrdinazioneDao();
    }
    @Override
    public DatabaseProdottoDao getProdottoDao() {
        return new DatabaseProdottoDao();
    }

    @Override
    public DatabaseOrdineDao getOrdineDao() {
        return new DatabaseOrdineDao();
    }

    @Override
    public DatabaseArchivioDao getArchivioDao() {
        return new DatabaseArchivioDao();
    }

}
