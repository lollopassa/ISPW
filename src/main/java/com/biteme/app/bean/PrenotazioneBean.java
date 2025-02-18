package com.biteme.app.bean;

import java.time.LocalDate;
import java.time.LocalTime;

public class PrenotazioneBean {
    private int id;
    private String nomeCliente;
    private LocalDate data;
    private LocalTime orario;
    private String orarioStr;
    private String note;
    private String email;
    private int coperti;
    private String copertiStr;

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

    public String getOrarioStr() {
        return orarioStr;
    }
    public void setOrarioStr(String orarioStr) {
        this.orarioStr = orarioStr;
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

    public int getCoperti() {
        return coperti;
    }
    public void setCoperti(int coperti) {
        this.coperti = coperti;
    }

    public String getCopertiStr() {
        return copertiStr;
    }
    public void setCopertiStr(String copertiStr) {
        this.copertiStr = copertiStr;
    }
}
