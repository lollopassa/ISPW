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

    /* -------- CREATE -------- */
    @Override
    public int create(Ordinazione o) {
        int id = (o.getId() > 0) ? o.getId() : idGenerator.getAndIncrement();

        // upsert
        ordinazioni.removeIf(ord -> ord.getId() == id);
        Ordinazione copia = new Ordinazione(
                id,
                o.getNomeCliente(), o.getNumeroClienti(),
                o.getTipoOrdine(), o.getInfoTavolo(),
                o.getStatoOrdine(), o.getOrarioCreazione());
        ordinazioni.add(copia);
        return id;
    }

    /* -------- READ -------- */
    @Override
    public Optional<Ordinazione> read(int id) {
        return ordinazioni.stream().filter(o -> o.getId() == id).findFirst();
    }

    /* -------- DELETE -------- */
    @Override
    public void delete(int id) {
        ordini.removeIf (o -> o.getId() == id);   // tavolo Ordine correlato
        ordinazioni.removeIf(o -> o.getId() == id);
    }

    /* -------- UPDATE STATO -------- */
    @Override
    public void aggiornaStato(int id, StatoOrdinazione nuovo) {
        read(id).ifPresent(o -> o.setStatoOrdine(nuovo));
    }

    /* -------- LISTA -------- */
    @Override
    public List<Ordinazione> getAll() {
        return ordinazioni;            // già lista live in memoria
    }

    /* exists() eredita l’implementazione di default dal DAO */
}
