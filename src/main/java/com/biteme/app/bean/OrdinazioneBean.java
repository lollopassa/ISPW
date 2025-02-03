package com.biteme.app.bean;

import com.biteme.app.entity.StatoOrdine;
import com.biteme.app.entity.TipoOrdine;

public class OrdinazioneBean {

    private int id; // BIGINT
    private String nome; // VARCHAR
    private String numeroClienti; // VARCHAR
    private TipoOrdine tipoOrdine; // VARCHAR
    private String infoTavolo; // VARCHAR
    private StatoOrdine statoOrdine; // Enum per lo stato dell'ordine
    private String orarioCreazione; // VARCHAR

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