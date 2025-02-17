package com.biteme.app.entities;

public class Ordinazione {

    private int id;
    private String nomeCliente;
    private String numeroClienti;
    private TipoOrdinazione tipoOrdinazione;
    private String infoTavolo;
    private StatoOrdinazione statoOrdinazione;
    private String orarioCreazione;

    // Costruttore completo
    public Ordinazione(int id, String nomeCliente, String numeroClienti, TipoOrdinazione tipoOrdinazione,
                       String infoTavolo, StatoOrdinazione statoOrdinazione, String orarioCreazione) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.numeroClienti = numeroClienti;
        this.tipoOrdinazione = tipoOrdinazione;
        this.infoTavolo = infoTavolo;
        this.statoOrdinazione = statoOrdinazione; // Cambiato a StatoOrdine
        this.orarioCreazione = orarioCreazione;
    }

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

    public TipoOrdinazione getTipoOrdine() {
        return tipoOrdinazione;
    }

    public void setTipoOrdine(TipoOrdinazione tipoOrdinazione) {
        this.tipoOrdinazione = tipoOrdinazione;
    }

    public String getInfoTavolo() {
        return infoTavolo;
    }

    public void setInfoTavolo(String infoTavolo) {
        this.infoTavolo = infoTavolo;
    }

    public StatoOrdinazione getStatoOrdine() {
        return statoOrdinazione;
    }

    public void setStatoOrdine(StatoOrdinazione statoOrdinazione) {
        this.statoOrdinazione = statoOrdinazione;
    }

    public String getOrarioCreazione() {
        return orarioCreazione;
    }

    public void setOrarioCreazione(String orarioCreazione) {
        this.orarioCreazione = orarioCreazione;
    }
}