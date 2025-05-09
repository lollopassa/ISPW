package com.biteme.app.bean;

import com.biteme.app.exception.OrdinazioneException;
import java.time.LocalTime;

public class OrdinazioneBean {

    private int id;
    private String nome;
    private String numeroClienti;
    private String tipoOrdine;
    private String infoTavolo;
    private String statoOrdine;
    private String orarioCreazione;

    public void validate() throws OrdinazioneException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new OrdinazioneException("Il campo Nome Cliente deve essere compilato.");
        }
        if (tipoOrdine == null || tipoOrdine.trim().isEmpty()) {
            throw new OrdinazioneException("Seleziona un tipo di ordine: 'Al Tavolo' o 'Asporto'.");
        }

        if ("Al Tavolo".equals(tipoOrdine)) {
            validateAlTavolo();
        } else {
            validateAsporto();
        }
    }

    private void validateAlTavolo() throws OrdinazioneException {
        if (numeroClienti == null || numeroClienti.trim().isEmpty()) {
            throw new OrdinazioneException("Il numero di coperti è obbligatorio per gli ordini 'Al Tavolo'.");
        }
        if (!numeroClienti.matches("\\d+")) {
            throw new OrdinazioneException("Il campo 'Numero di Coperti' deve contenere solo numeri interi.");
        }
        if (infoTavolo == null || infoTavolo.trim().isEmpty()) {
            throw new OrdinazioneException("Il numero del tavolo è obbligatorio per gli ordini 'Al Tavolo'.");
        }
        this.orarioCreazione = LocalTime.now().toString().substring(0, 5);
    }


    private void validateAsporto() throws OrdinazioneException {
        if (orarioCreazione == null || orarioCreazione.trim().isEmpty()) {
            throw new OrdinazioneException("Il campo Orario deve essere compilato per Asporto.");
        }
        String timePattern = "([01]\\d|2[0-3]):([0-5]\\d)";
        if (!orarioCreazione.matches(timePattern)) {
            throw new OrdinazioneException("Il campo 'Orario' deve essere nel formato HH:mm (es. '12:20').");
        }
        this.numeroClienti = "";
        this.infoTavolo = "";
    }



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