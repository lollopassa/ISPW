package com.biteme.app.bean;

import java.time.LocalDate;
import java.time.LocalTime;

public class PrenotazioneBean {
    private int id;
    private String nomeCliente;
    private LocalDate data;
    private LocalTime orario;
    private String note;
    private String email;
    private Integer coperti;

    public int getId() {
        return id; }
    public void setId(int id) {
        this.id = id; }

    public String getNomeCliente() {
        return nomeCliente; }
    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente; }

    public LocalDate getData() {
        return data; }
    public void setData(LocalDate data) {
        this.data = data; }

    public LocalTime getOrario() {
        return orario; }
    public void setOrario(LocalTime orario) {
        this.orario = orario; }

    public String getNote() {
        return note; }
    public void setNote(String note) {
        this.note = note; }

    public String getEmail() {
        return email; }
    public void setEmail(String email) {
        this.email = email; }

    public Integer getCoperti() {
        return coperti; }
    public void setCoperti(Integer coperti) {
        this.coperti = coperti; }
}
