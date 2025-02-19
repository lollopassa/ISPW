package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Storage {

    protected static Storage instance;

        private final List<Prenotazione> prenotazioni = Collections.synchronizedList(new ArrayList<>());

        private final List<Ordine> ordini = Collections.synchronizedList(new ArrayList<>());

        private final List<Ordinazione> ordinazioni = Collections.synchronizedList(new ArrayList<>());

        private final List<Prodotto> prodotti = Collections.synchronizedList(new ArrayList<>());

        private final Map<String, User> users = new HashMap<>();

    private final List<Archivio> archivi = new ArrayList<>();

    protected Storage() {
            }

    public static synchronized Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

        public List<Prenotazione> getPrenotazioni() {
        return prenotazioni;
    }

        public List<Ordinazione> getOrdinazioni() {
        return ordinazioni;
    }

        public List<Ordine> getOrdini() {
        return ordini;
    }

        public List<Prodotto> getProdotti() {
        return prodotti;
    }

        public Map<String, User> getUsers() {
        return users;
    }

    public List<Archivio> getArchivi() {
        return archivi;
    }
}