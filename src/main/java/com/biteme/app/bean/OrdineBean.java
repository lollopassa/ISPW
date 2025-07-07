package com.biteme.app.bean;

import com.biteme.app.exception.OrdineException;

import java.math.BigDecimal;
import java.util.List;

public class OrdineBean {

    /* ---------- campi ---------- */
    private int                 id;          // = id dell’ordinazione
    private List<String>        prodotti;    // nomi
    private List<Integer>       quantita;    // quantità corrispondenti
    private List<BigDecimal>    prezzi;      // prezzi unitari (sempre presenti – 0 € per gli “extra”)

    /* ---------- getter / setter ---------- */
    public int              getId()                { return id; }
    public void             setId(int id)          { this.id = id; }

    public List<String>     getProdotti()          { return prodotti; }
    public void             setProdotti(List<String> prodotti) { this.prodotti = prodotti; }

    public List<Integer>    getQuantita()          { return quantita; }
    public void             setQuantita(List<Integer> quantita) { this.quantita = quantita; }

    public List<BigDecimal> getPrezzi()            { return prezzi; }
    public void             setPrezzi(List<BigDecimal> prezzi)  { this.prezzi = prezzi; }

    /* ---------- validazione ---------- */
    public void validate() throws OrdineException {
        assureNotNull();
        assureSameSize();
        assureQuantitiesPositive();
        assurePricesPositive();
    }

    /* --- step 1: mai liste null --- */
    private void assureNotNull() throws OrdineException {
        if (prodotti == null || quantita == null || prezzi == null)
            throw new OrdineException("Prodotti, quantità e prezzi non possono essere null.");
    }

    /* --- step 2: lunghezze allineate --- */
    private void assureSameSize() throws OrdineException {
        int n = prodotti.size();
        if (n != quantita.size() || n != prezzi.size())
            throw new OrdineException("Prodotti, quantità e prezzi devono avere la stessa cardinalità.");
    }

    /* --- step 3: quantità > 0 --- */
    private void assureQuantitiesPositive() throws OrdineException {
        for (Integer q : quantita)
            if (q == null || q <= 0)
                throw new OrdineException("Ogni quantità deve essere maggiore di zero.");
    }

    /* --- step 4: prezzi ≥ 0 --- */
    private void assurePricesPositive() throws OrdineException {
        for (BigDecimal p : prezzi)
            if (p == null || p.compareTo(BigDecimal.ZERO) < 0)
                throw new OrdineException("Ogni prezzo deve essere ≥ 0.");
    }

    /* ---------- utilità ---------- */
    /** Serve eventualmente alla UI per capire se i prezzi sono già stati popolati. */
    public boolean isPrezziPresenti() {
        return prezzi != null && !prezzi.isEmpty();
    }
}
