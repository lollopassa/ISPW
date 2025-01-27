package com.biteme.app.bean;

import java.time.LocalDate;
import java.time.LocalTime;

public class PrenotazioniBean {
    private String nomeCliente;
    private LocalDate data; // Nuovo campo per gestire la data della prenotazione
    private LocalTime orario;
    private String note; // Rinominato il campo da "dettagli" a "note"
    private String telefono;
    private Integer coperti; // Nuovo campo per il numero di coperti

    // Getter e Setter
    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getOrario() {
        return orario;
    }

    public void setOrario(LocalTime orario) {
        this.orario = orario;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Integer getCoperti() {
        return coperti;
    }

    public void setCoperti(Integer coperti) {
        this.coperti = coperti;
    }
}