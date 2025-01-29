package com.biteme.app.persistence.inmemory;

import com.biteme.app.persistence.*;

public class InMemoryDaoFactory extends DaoFactory {

    @Override
    public UserDao getUserDao() {
        return new InMemoryUserDao();
    }

    @Override
    public PrenotazioneDao getPrenotazioneDao() {
        return new InMemoryPrenotazioneDao();
    }

    public  OrdinazioneDao getOrdinazioneDao() {
        return new InMemoryOrdinazioneDao();
    }

}