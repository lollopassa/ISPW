package com.biteme.app.bean;

public class OrdinazioneBean {

    private int id; // BIGINT
    private String nomeCliente; // VARCHAR
    private String numeroClienti; // VARCHAR
    private String tipoOrdine; // VARCHAR
    private String infoTavolo; // VARCHAR
    private String statoOrdine; // VARCHAR
    private String orarioCreazione; // VARCHAR

    // Getter e Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
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
