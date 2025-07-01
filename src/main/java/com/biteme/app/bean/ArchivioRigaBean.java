package com.biteme.app.bean;

public class ArchivioRigaBean {
    private ProdottoBean prodottoBean;
    private Integer quantita;

    public ProdottoBean getProdottoBean() { return prodottoBean; }
    public void setProdottoBean(ProdottoBean prodottoBean) {
        this.prodottoBean = prodottoBean;
    }

    public Integer getQuantita() { return quantita; }
    public void setQuantita(Integer quantita) { this.quantita = quantita; }
}