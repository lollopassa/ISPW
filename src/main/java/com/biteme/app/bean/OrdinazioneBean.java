package com.biteme.app.bean;

import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.entities.TipoOrdinazione;
import com.biteme.app.exception.OrdinazioneException;
import java.time.LocalTime;

public class OrdinazioneBean {

    private int    id;               // PK
    private String nome;             // nome cliente
    private String numeroClienti;    // coperti (stringa per validazione UI)
    private String tipoOrdine;       // "Al Tavolo" / "Asporto"
    private String infoTavolo;       // numero tavolo
    private String statoOrdine;      // stato in testo
    private String orarioCreazione;  // HH:mm

    public void validate() throws OrdinazioneException {
        if (nome == null || nome.trim().isEmpty())            throw new OrdinazioneException("Il campo Nome Cliente deve essere compilato.");
        if (tipoOrdine == null || tipoOrdine.trim().isEmpty()) throw new OrdinazioneException("Seleziona un tipo di ordine: 'Al Tavolo' o 'Asporto'.");
        if ("Al Tavolo".equals(tipoOrdine)) validateAlTavolo(); else validateAsporto();
    }

    private void validateAlTavolo() throws OrdinazioneException {
        if (numeroClienti == null || numeroClienti.trim().isEmpty()) throw new OrdinazioneException("Il numero di coperti è obbligatorio per gli ordini 'Al Tavolo'.");
        if (!numeroClienti.matches("\\d+"))                     throw new OrdinazioneException("Il campo 'Numero di Coperti' deve contenere solo numeri interi.");
        if (infoTavolo == null || infoTavolo.trim().isEmpty())     throw new OrdinazioneException("Il numero del tavolo è obbligatorio per gli ordini 'Al Tavolo'.");
        this.orarioCreazione = LocalTime.now().toString().substring(0, 5); // orario corrente HH:mm
    }

    private void validateAsporto() throws OrdinazioneException {
        if (orarioCreazione == null || orarioCreazione.trim().isEmpty()) throw new OrdinazioneException("Il campo Orario deve essere compilato per Asporto.");
        if (!orarioCreazione.matches("([01]\\d|2[0-3]):([0-5]\\d)")) throw new OrdinazioneException("Il campo 'Orario' deve essere nel formato HH:mm (es. '12:20').");
        this.numeroClienti = ""; // non rilevante per asporto
        this.infoTavolo    = ""; // non rilevante per asporto
    }

    public Ordinazione toEntity() { // DTO → entity
        TipoOrdinazione tipoEnum = "Al Tavolo".equals(tipoOrdine) ? TipoOrdinazione.AL_TAVOLO : TipoOrdinazione.ASPORTO;
        String nClienti = (numeroClienti == null || numeroClienti.isBlank()) ? "" : numeroClienti; // già validata
        return new Ordinazione(id, nome, nClienti, tipoEnum, infoTavolo, StatoOrdinazione.NUOVO, orarioCreazione);
    }

    public static OrdinazioneBean fromEntity(Ordinazione o) { // entity → DTO
        OrdinazioneBean b = new OrdinazioneBean();
        b.setId(o.getId());
        b.setNome(o.getNomeCliente());
        b.setNumeroClienti(String.valueOf(o.getNumeroClienti()));
        b.setTipoOrdine(o.getTipoOrdine().toString().replace('_', ' '));
        b.setInfoTavolo(o.getInfoTavolo());
        b.setStatoOrdine(o.getStatoOrdine().toString());
        b.setOrarioCreazione(o.getOrarioCreazione());
        return b;
    }

    // getter e setter compact:
    public int getId() { return id; }                 public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }          public void setNome(String nome) { this.nome = nome; }
    public String getNumeroClienti() { return numeroClienti; } public void setNumeroClienti(String numeroClienti) { this.numeroClienti = numeroClienti; }
    public String getTipoOrdine() { return tipoOrdine; }       public void setTipoOrdine(String tipoOrdine) { this.tipoOrdine = tipoOrdine; }
    public String getInfoTavolo() { return infoTavolo; }       public void setInfoTavolo(String infoTavolo) { this.infoTavolo = infoTavolo; }
    public String getStatoOrdine() { return statoOrdine; }     public void setStatoOrdine(String statoOrdine) { this.statoOrdine = statoOrdine; }
    public String getOrarioCreazione() { return orarioCreazione; } public void setOrarioCreazione(String orarioCreazione) { this.orarioCreazione = orarioCreazione; }
}
