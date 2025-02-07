package com.biteme.app.bean;

public class OrdinazioneBean {

    private int id;
    private String nome;
    private String numeroClienti;
    // Ora usiamo String anziché il model enum TipoOrdine
    private String tipoOrdine;
    private String infoTavolo;
    // Ora usiamo String anziché il model enum StatoOrdine
    private String statoOrdine;
    private String orarioCreazione;

    // Getter e Setter
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getNumeroClienti() {
        return numeroClienti;
    }
    public void setNumeroClienti(String numeroClienti) {
        this.numeroClienti = numeroClienti;
    }
    public String getTipoOrdine() {
        return tipoOrdine;
    }
    public void setTipoOrdine(String tipoOrdine) {
        this.tipoOrdine = tipoOrdine;
    }
    public String getInfoTavolo() {
        return infoTavolo;
    }
    public void setInfoTavolo(String infoTavolo) {
        this.infoTavolo = infoTavolo;
    }
    public String getStatoOrdine() {
        return statoOrdine;
    }
    public void setStatoOrdine(String statoOrdine) {
        this.statoOrdine = statoOrdine;
    }
    public String getOrarioCreazione() {
        return orarioCreazione;
    }
    public void setOrarioCreazione(String orarioCreazione) {
        this.orarioCreazione = orarioCreazione;
    }
}
