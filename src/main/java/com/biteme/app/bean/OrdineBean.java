package com.biteme.app.bean;

import com.biteme.app.exception.OrdineException;

import java.math.BigDecimal;
import java.util.List;


public class OrdineBean {

    private int id;
    private List<String> prodotti;
    private List<Integer> quantita;
    private List<BigDecimal> prezzi;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public List<String> getProdotti() { return prodotti; }
    public void setProdotti(List<String> prodotti) { this.prodotti = prodotti; }

    public List<Integer> getQuantita() { return quantita; }
    public void setQuantita(List<Integer> quantita) { this.quantita = quantita; }

    public List<BigDecimal> getPrezzi() {        return prezzi;    }
    public void setPrezzi(List<BigDecimal> prezzi) {        this.prezzi = prezzi;    }


    public void validate() throws OrdineException {
        if (prodotti == null || quantita == null || prezzi == null) {
            throw new OrdineException("Prodotti, quantità e prezzi non possono essere null.");
        }
        int n = prodotti.size();
        if (n != quantita.size() || n != prezzi.size()) {
            throw new OrdineException("Numero di prodotti, quantità e prezzi non corrispondono.");
        }
        for (Integer q : quantita) {
            if (q == null || q < 0) {
                throw new OrdineException("Ogni quantità deve essere un intero ≥ 0.");
            }
        }
        for (BigDecimal p : prezzi) {
            if (p == null || p.compareTo(BigDecimal.ZERO) < 0) {
                throw new OrdineException("Ogni prezzo deve essere ≥ 0.");
            }
        }
    }

}
