package com.biteme.app.entity;

public class Ordinazione {

    private int id; // INT nel database
    private String nomeCliente; // VARCHAR
    private String numeroClienti; // VARCHAR (lasciato così com'è)
    private TipoOrdine tipoOrdine; // VARCHAR
    private String infoTavolo; // VARCHAR
    private StatoOrdine statoOrdine; // Enum StatoOrdine
    private String orarioCreazione; // VARCHAR

    // Costruttore completo
    public Ordinazione(int id, String nomeCliente, String numeroClienti, TipoOrdine tipoOrdine,
                       String infoTavolo, StatoOrdine statoOrdine, String orarioCreazione) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.numeroClienti = numeroClienti;
        this.tipoOrdine = tipoOrdine;
        this.infoTavolo = infoTavolo;
        this.statoOrdine = statoOrdine; // Cambiato a StatoOrdine
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

    public TipoOrdine getTipoOrdine() {
        return tipoOrdine;
    }

    public void setTipoOrdine(TipoOrdine tipoOrdine) {
        this.tipoOrdine = tipoOrdine;
    }

    public String getInfoTavolo() {
        return infoTavolo;
    }

    public void setInfoTavolo(String infoTavolo) {
        this.infoTavolo = infoTavolo;
    }

    public StatoOrdine getStatoOrdine() {
        return statoOrdine;
    }

    public void setStatoOrdine(StatoOrdine statoOrdine) {
        this.statoOrdine = statoOrdine;
    }

    public String getOrarioCreazione() {
        return orarioCreazione;
    }

    public void setOrarioCreazione(String orarioCreazione) {
        this.orarioCreazione = orarioCreazione;
    }
}