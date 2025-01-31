package com.biteme.app.persistence.inmemory;

import com.biteme.app.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Storage {

    private static Storage instance;

    // Lista sincronizzata per le prenotazioni
    private final List<Prenotazione> prenotazioni = Collections.synchronizedList(new ArrayList<>());

    // Lista sincronizzata per gli ordini
    private final List<Ordine> ordini = Collections.synchronizedList(new ArrayList<>());

    // Lista sincronizzata per le ordinazioni
    private final List<Ordinazione> ordinazioni = Collections.synchronizedList(new ArrayList<>());

    // Lista sincronizzata per i prodotti
    private final List<Prodotto> prodotti = Collections.synchronizedList(new ArrayList<>());

    // Mappa per memorizzare gli utenti
    private final Map<String, User> users = new HashMap<>();

    private Storage() {
        // Costruttore privato per implementare il Singleton
    }

    public static synchronized Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    // Accesso alla lista delle prenotazioni
    public List<Prenotazione> getPrenotazioni() {
        return prenotazioni;
    }

    // Accesso alla lista delle ordinazioni
    public List<Ordinazione> getOrdinazioni() {
        return ordinazioni;
    }

    // Accesso alla lista degli ordini
    public List<Ordine> getOrdini() {
        return ordini;
    }

    // Accesso alla lista dei prodotti
    public List<Prodotto> getProdotti() {
        return prodotti;
    }

    // Accesso alla mappa degli utenti
    public Map<String, User> getUsers() {
        return users;
    }
}