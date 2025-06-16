package com.biteme.app.bean;

import com.biteme.app.entities.Ordine;
import com.biteme.app.exception.OrdineException;

import java.math.BigDecimal;
import java.util.List;


public class OrdineBean {


    private int id;                              // PK ordine = id ordinazione
    private List<String> prodotti;               // elenco nomi prodotti
    private List<Integer> quantita;              // quantità corrispondenti
    private List<BigDecimal> prezzi;             // prezzi unitari opzionali

    // getter/setter essenziali
    public int getId() { return id; }            public void setId(int id) { this.id = id; }
    public List<String> getProdotti() { return prodotti; }   public void setProdotti(List<String> p) { this.prodotti = p; }
    public List<Integer> getQuantita() { return quantita; }  public void setQuantita(List<Integer> q) { this.quantita = q; }
    public List<BigDecimal> getPrezzi() { return prezzi; }   public void setPrezzi(List<BigDecimal> pr) { this.prezzi = pr; }

    // validazione di coerenza interna
    public void validate() throws OrdineException {
        validateNotNull();
        validateSizes();
        validateQuantities();
        validatePricesIfPresent();
    }
    private void validateNotNull() throws OrdineException {
        if (prodotti == null || quantita == null) throw new OrdineException("Prodotti e quantità non possono essere null.");
    }
    private void validateSizes() throws OrdineException {
        int n = prodotti.size();
        if (n != quantita.size()) throw new OrdineException("Prodotti e quantità non corrispondono.");
        if (prezzi != null && n != prezzi.size()) throw new OrdineException("Numero prezzi non allineato ai prodotti.");
    }
    private void validateQuantities() throws OrdineException {
        for (Integer q : quantita) if (q == null || q < 0) throw new OrdineException("Ogni quantità dev’essere ≥ 0.");
    }
    private void validatePricesIfPresent() throws OrdineException {
        if (prezzi == null) return; // prezzi facoltativi
        for (BigDecimal p : prezzi) if (p == null || p.compareTo(BigDecimal.ZERO) < 0) throw new OrdineException("Ogni prezzo dev’essere ≥ 0.");
    }

    // mapping DTO ↔ entity
    public Ordine toEntity(int id) { return new Ordine(id, prodotti, quantita, prezzi); }
    public static OrdineBean fromEntity(Ordine o) {
        OrdineBean b = new OrdineBean();
        b.setId(o.getId());
        b.setProdotti(o.getProdotti());
        b.setQuantita(o.getQuantita());
        b.setPrezzi(o.getPrezzi());
        return b;
    }

    // utilità rapida
    public boolean isPrezziPresenti() { return prezzi != null && !prezzi.isEmpty(); }
}