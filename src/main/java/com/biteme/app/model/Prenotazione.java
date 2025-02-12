package com.biteme.app.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Prenotazione {

    private int id;
    private String nomeCliente;
    private LocalTime orario;
    private LocalDate data;
    private String note;
    private String telefono;
    private int coperti;

    public Prenotazione(int id, String nomeCliente, LocalTime orario, LocalDate data, String note, String telefono, int coperti) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.orario = orario;
        this.data = data;
        this.note = note;
        this.telefono = telefono;
        this.coperti = coperti;
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

    public LocalTime getOrario() {
        return orario;
    }

    public void setOrario(LocalTime orario) {
        this.orario = orario;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
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

    public int getCoperti() { // NUOVO CAMPO
        return coperti;
    }

    public void setCoperti(int coperti) {
        this.coperti = coperti;
    }
}