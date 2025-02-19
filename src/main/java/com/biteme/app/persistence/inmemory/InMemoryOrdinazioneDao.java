package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.Ordine;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.entities.StatoOrdinazione;

import java.util.List;
import java.util.Optional;

public class InMemoryOrdinazioneDao implements OrdinazioneDao {

    private final List<Ordine> ordini = Storage.getInstance().getOrdini();
    private int currentId = 1;
    private final List<Ordinazione> ordinazioni = Storage.getInstance().getOrdinazioni();
    @Override
    public Optional<Ordinazione> load(Integer key) {
        return ordinazioni.stream()
                .filter(o -> o.getId() == key)                 .findFirst();
    }

    @Override
    public void store(Ordinazione ordinazione) {
                if (ordinazione.getId() > 0 && exists(ordinazione.getId())) {
            delete(ordinazione.getId());
        } else if (ordinazione.getId() <= 0) {
                        ordinazione.setId(currentId++);
        }
        ordinazioni.add(ordinazione);
    }

    @Override
    public void delete(Integer key) {
        if (exists(key)) {                         ordini.removeIf(o -> o.getId() == key);

                        ordinazioni.removeIf(o -> o.getId() == key);
        }
    }

    @Override
    public boolean exists(Integer key) {
                return ordinazioni.stream().anyMatch(o -> o.getId() == key);
    }

    @Override
    public List<Ordinazione> getAll() {
                return ordinazioni;
    }

    @Override
    public void aggiornaStato(int id, StatoOrdinazione nuovoStato) {
                ordinazioni.stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .ifPresent(ordinazione -> ordinazione.setStatoOrdine(nuovoStato));
    }
}