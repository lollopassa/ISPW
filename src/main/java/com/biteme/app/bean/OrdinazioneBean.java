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

    // Getters e setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getNumeroClienti() { return numeroClienti; }
    public void setNumeroClienti(String numeroClienti) { this.numeroClienti = numeroClienti; }

    public String getTipoOrdine() { return tipoOrdine; }
    public void setTipoOrdine(String tipoOrdine) { this.tipoOrdine = tipoOrdine; }

    public String getInfoTavolo() { return infoTavolo; }
    public void setInfoTavolo(String infoTavolo) { this.infoTavolo = infoTavolo; }

    public String getStatoOrdine() { return statoOrdine; }
    public void setStatoOrdine(String statoOrdine) { this.statoOrdine = statoOrdine; }

    public String getOrarioCreazione() { return orarioCreazione; }
    public void setOrarioCreazione(String orarioCreazione) { this.orarioCreazione = orarioCreazione; }


    public void validate() throws OrdinazioneException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new OrdinazioneException("Il campo Nome Cliente deve essere compilato.");
        }
        if (tipoOrdine == null || tipoOrdine.trim().isEmpty()) {
            throw new OrdinazioneException("Seleziona un tipo di ordine: 'Al Tavolo' o 'Asporto'.");
        }
        if ("Al Tavolo".equals(tipoOrdine)) {
            if (numeroClienti == null || numeroClienti.trim().isEmpty()) {
                throw new OrdinazioneException("Il numero di coperti è obbligatorio per gli ordini 'Al Tavolo'.");
            }
            if (!numeroClienti.matches("\\d+")) {
                throw new OrdinazioneException("Il campo 'Numero di Coperti' deve contenere solo numeri interi.");
            }
            if (infoTavolo == null || infoTavolo.trim().isEmpty()) {
                throw new OrdinazioneException("Il numero del tavolo è obbligatorio per gli ordini 'Al Tavolo'.");
            }
            orarioCreazione = LocalTime.now().toString().substring(0, 5);
        } else if ("Asporto".equals(tipoOrdine)) {
            if (orarioCreazione == null || orarioCreazione.trim().isEmpty()) {
                throw new OrdinazioneException("Il campo Orario deve essere compilato per Asporto.");
            }
            String timePattern = "([01]\\d|2[0-3]):([0-5]\\d)";
            if (!orarioCreazione.matches(timePattern)) {
                throw new OrdinazioneException("Il campo 'Orario' deve essere nel formato HH:mm (es. '12:20').");
            }
            numeroClienti = "";
            infoTavolo = "";
        } else {
            throw new OrdinazioneException("Tipo di ordine non valido: " + tipoOrdine);
        }
    }
}
