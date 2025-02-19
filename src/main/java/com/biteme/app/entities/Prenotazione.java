package com.biteme.app.entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class Prenotazione {

    private int id;
    private String nomeCliente;
    private LocalTime orario;
    private LocalDate data;
    private String note;
    private String email;
    private int coperti;

    public Prenotazione(int id, String nomeCliente, LocalTime orario, LocalDate data, String note, String email, int coperti) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.orario = orario;
        this.data = data;
        this.note = note;
        this.email = email;
        this.coperti = coperti;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCoperti() {         return coperti;
    }

    public void setCoperti(int coperti) {
        this.coperti = coperti;
    }
}