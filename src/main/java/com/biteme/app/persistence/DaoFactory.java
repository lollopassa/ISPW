package com.biteme.app.persistence;


public abstract class DaoFactory {

    public abstract UserDao getUserDao();

    public abstract PrenotazioneDao getPrenotazioneDao();

    public abstract OrdinazioneDao getOrdinazioneDao();

    public abstract ProdottoDao getProdottoDao();

    public abstract OrdineDao getOrdineDao();

    public abstract ArchivioDao getArchivioDao();

}
