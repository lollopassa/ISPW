package com.biteme.app.bean;

import com.biteme.app.exception.OrdinazioneException;

import java.time.LocalTime;

/** DTO per i form di creazione/aggiornamento ordini. */
public class OrdinazioneBean {

    private int    id;
    private String nome;
    private String numeroClienti;   // testo proveniente dal form
    private String tipoOrdine;      // "Al Tavolo" / "Asporto"
    private String infoTavolo;
    private String statoOrdine;     // opzionale, impostato dal server
    private String orarioCreazione; // HH:mm

    /*------------- VALIDAZIONE -------------*/
    public void validate() throws OrdinazioneException {
        if (nome == null || nome.trim().isEmpty())
            throw new OrdinazioneException("Il campo Nome Cliente deve essere compilato.");

        if (tipoOrdine == null || tipoOrdine.trim().isEmpty())
            throw new OrdinazioneException("Seleziona un tipo di ordine: 'Al Tavolo' o 'Asporto'.");

        if ("Al Tavolo".equals(tipoOrdine))      validateAlTavolo();
        else if ("Asporto".equals(tipoOrdine))   validateAsporto();
        else throw new OrdinazioneException("Tipo ordine non valido.");
    }

    private void validateAlTavolo() throws OrdinazioneException {
        if (numeroClienti == null || numeroClienti.trim().isEmpty())
            throw new OrdinazioneException("Il numero di coperti è obbligatorio per gli ordini 'Al Tavolo'.");
        if (!numeroClienti.matches("\\d+"))
            throw new OrdinazioneException("Il campo 'Numero di Coperti' deve contenere solo numeri interi.");
        if (infoTavolo == null || infoTavolo.trim().isEmpty())
            throw new OrdinazioneException("Il numero del tavolo è obbligatorio per gli ordini 'Al Tavolo'.");
        if (orarioCreazione == null || orarioCreazione.isBlank())
            orarioCreazione = LocalTime.now().toString().substring(0,5);
    }

    private void validateAsporto() throws OrdinazioneException {
        if (orarioCreazione == null || orarioCreazione.trim().isEmpty())
            throw new OrdinazioneException("Il campo Orario deve essere compilato per Asporto.");
        if (!orarioCreazione.matches("([01]\\d|2[0-3]):([0-5]\\d)"))
            throw new OrdinazioneException("Il campo 'Orario' deve essere nel formato HH:mm (es. '12:20').");
        // ** NON azzeriamo più numeroClienti e infoTavolo **
    }

    /*------------- getter / setter compact -------------*/
    public int getId() { return id; }            public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }     public void setNome(String nome) { this.nome = nome; }
    public String getNumeroClienti() { return numeroClienti; } public void setNumeroClienti(String n) { this.numeroClienti = n; }
    public String getTipoOrdine() { return tipoOrdine; }       public void setTipoOrdine(String t) { this.tipoOrdine = t; }
    public String getInfoTavolo() { return infoTavolo; }       public void setInfoTavolo(String t) { this.infoTavolo = t; }
    public String getStatoOrdine() { return statoOrdine; }     public void setStatoOrdine(String s) { this.statoOrdine = s; }
    public String getOrarioCreazione() { return orarioCreazione; } public void setOrarioCreazione(String o) { this.orarioCreazione = o; }
}
