package com.biteme.app.bean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import com.biteme.app.exception.PrenotationValidationException;

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

    // Getter e setter esistenti

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


    public void validate() {
        if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
            throw new PrenotationValidationException("Il nome del cliente non può essere vuoto.");
        }
        if (data == null) {
            throw new PrenotationValidationException("Seleziona una data valida.");
        }
        if (orarioStr == null || orarioStr.trim().isEmpty()) {
            throw new PrenotationValidationException("Inserisci un orario valido.");
        } else {
            try {
                LocalTime parsedOrario = LocalTime.parse(orarioStr.trim());
                this.orario = parsedOrario;
            } catch (DateTimeParseException e) {
                throw new PrenotationValidationException("Formato orario non valido. Usa 'HH:mm'.");
            }
        }
        if (copertiStr == null || copertiStr.trim().isEmpty()) {
            throw new PrenotationValidationException("Inserisci il numero di coperti.");
        } else {
            try {
                int parsedCoperti = Integer.parseInt(copertiStr.trim());
                if (parsedCoperti <= 0) {
                    throw new PrenotationValidationException("I coperti devono essere maggiori di 0.");
                }
                this.coperti = parsedCoperti;
            } catch (NumberFormatException e) {
                throw new PrenotationValidationException("Numero coperti non valido.");
            }
        }
        if (email != null && !email.trim().isEmpty() &&
                !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,7}$")) {
            throw new PrenotationValidationException("Formato email non valido.");
        }
    }
}
