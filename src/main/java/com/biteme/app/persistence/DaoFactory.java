package com.biteme.app.persistence;


public abstract class DaoFactory {

    public abstract UserDao getUserDao();

    public abstract PrenotazioneDao getPrenotazioneDao();
}