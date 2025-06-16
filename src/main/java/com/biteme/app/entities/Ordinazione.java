package com.biteme.app.entities;

public class Ordinazione {

    private int id;
    private String nomeCliente;
    private String numeroClienti;
    private TipoOrdinazione tipoOrdinazione;
    private String infoTavolo;
    private StatoOrdinazione statoOrdinazione;
    private String orarioCreazione;

        public Ordinazione(int id, String nomeCliente, String numeroClienti, TipoOrdinazione tipoOrdinazione,
                       String infoTavolo, StatoOrdinazione statoOrdinazione, String orarioCreazione) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.numeroClienti = numeroClienti;
        this.tipoOrdinazione = tipoOrdinazione;
        this.infoTavolo = infoTavolo;
        this.statoOrdinazione = statoOrdinazione;         this.orarioCreazione = orarioCreazione;
    }

        public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public String getNumeroClienti() {
        return numeroClienti;
    }

    public TipoOrdinazione getTipoOrdine() {
        return tipoOrdinazione;
    }

    public String getInfoTavolo() {
        return infoTavolo;
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

}