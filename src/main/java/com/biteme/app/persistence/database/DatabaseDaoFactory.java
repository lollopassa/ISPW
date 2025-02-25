package com.biteme.app.persistence.database;

import com.biteme.app.persistence.*;


public class DatabaseDaoFactory extends DaoFactory {


    @Override
    public UserDao getUserDao() {
        return new DatabaseUserDao();
    }

    @Override
    public PrenotazioneDao getPrenotazioneDao() {
        return new DatabasePrenotazioneDao();
    }

    @Override
    public OrdinazioneDao getOrdinazioneDao() {
        return new DatabaseOrdinazioneDao();
    }

    @Override
    public ProdottoDao getProdottoDao() {
        return new DatabaseProdottoDao();
    }

    @Override
    public OrdineDao getOrdineDao() {
        return new DatabaseOrdineDao();
    }

    @Override
    public ArchivioDao getArchivioDao() {
        return new DatabaseArchivioDao();
    }

}
