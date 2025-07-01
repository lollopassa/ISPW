package com.biteme.app.bean;

import com.biteme.app.exception.OrdineException;

import java.math.BigDecimal;
import java.util.List;

/** DTO usato dalla UI per creare / visualizzare un Ordine. */
public class OrdineBean {

    private int id;                          // PK = id ordinazione
    private List<String>    prodotti;        // nomi
    private List<Integer>   quantita;        // relative quantità
    private List<BigDecimal> prezzi;         // unitari (opz.)

    /* ---------- getter / setter ---------- */
    public int getId() { return id; }                     public void setId(int id) { this.id = id; }
    public List<String> getProdotti() { return prodotti; }public void setProdotti(List<String> p) { this.prodotti = p; }
    public List<Integer> getQuantita() { return quantita; }public void setQuantita(List<Integer> q) { this.quantita = q; }
    public List<BigDecimal> getPrezzi() { return prezzi; }public void setPrezzi(List<BigDecimal> pr) { this.prezzi = pr; }

    /* ---------- validazione ---------- */
    public void validate() throws OrdineException {
        validateNotNull();
        validateSizes();
        validateQuantities();
        validatePricesIfPresent();
    }
    private void validateNotNull() throws OrdineException {
        if (prodotti == null || quantita == null)
            throw new OrdineException("Prodotti e quantità non possono essere null.");
    }
    private void validateSizes() throws OrdineException {
        int n = prodotti.size();
        if (n != quantita.size())
            throw new OrdineException("Prodotti e quantità non corrispondono.");
        if (prezzi != null && n != prezzi.size())
            throw new OrdineException("Numero prezzi non allineato ai prodotti.");
    }
    private void validateQuantities() throws OrdineException {
        for (Integer q : quantita)
            if (q == null || q < 0)
                throw new OrdineException("Ogni quantità dev’essere ≥ 0.");
    }
    private void validatePricesIfPresent() throws OrdineException {
        if (prezzi == null) return;                // prezzi facoltativi
        for (BigDecimal p : prezzi)
            if (p == null || p.compareTo(BigDecimal.ZERO) < 0)
                throw new OrdineException("Ogni prezzo dev’essere ≥ 0.");
    }

    /* utility */
    public boolean isPrezziPresenti() {
        return prezzi != null && !prezzi.isEmpty();
    }
}
