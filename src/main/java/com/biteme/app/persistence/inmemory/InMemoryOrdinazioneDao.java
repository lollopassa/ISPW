package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.Ordine;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.persistence.OrdinazioneDao;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryOrdinazioneDao implements OrdinazioneDao {

    private final List<Ordine>       ordini       = Storage.getInstance().getOrdini();
    private final List<Ordinazione>  ordinazioni  = Storage.getInstance().getOrdinazioni();
    private final AtomicInteger idGenerator = new AtomicInteger(
            ordinazioni.stream().mapToInt(Ordinazione::getId).max().orElse(0) + 1);

    @Override
    public int create(Ordinazione o) {
        int id = (o.getId() > 0) ? o.getId() : idGenerator.getAndIncrement();

        ordinazioni.removeIf(ord -> ord.getId() == id);
        Ordinazione copia = new Ordinazione(
                id,
                o.getNomeCliente(), o.getNumeroClienti(),
                o.getTipoOrdine(), o.getInfoTavolo(),
                o.getStatoOrdine(), o.getOrarioCreazione());
        ordinazioni.add(copia);
        return id;
    }

    @Override
    public Optional<Ordinazione> read(int id) {
        return ordinazioni.stream().filter(o -> o.getId() == id).findFirst();
    }

    @Override
    public void delete(int id) {
        ordini.removeIf (o -> o.getId() == id);
        ordinazioni.removeIf(o -> o.getId() == id);
    }

    @Override
    public void aggiornaStato(int id, StatoOrdinazione nuovo) {
        read(id).ifPresent(o -> o.setStatoOrdine(nuovo));
    }

    @Override
    public List<Ordinazione> getAll() {
        return ordinazioni;
    }

}