package com.biteme.app.persistence.txt;

import com.biteme.app.persistence.*;

public class TxtDaoFactory extends DaoFactory {
    @Override
    public UserDao getUserDao() {
        return new TxtUserDao();
    }

    @Override
    public PrenotazioneDao getPrenotazioneDao() {
        return new TxtPrenotazioneDao();
    }

    @Override
    public OrdinazioneDao getOrdinazioneDao() {
        return new TxtOrdinazioneDao();
    }

    @Override
    public ProdottoDao getProdottoDao() {
        return new TxtProdottoDao();
    }

    @Override
    public OrdineDao getOrdineDao() {
        return new TxtOrdineDao();
    }

    @Override
    public ArchivioDao getArchivioDao() {
        return new TxtArchivioDao();
    }
}