package com.biteme.app.bean;

import java.math.BigDecimal;

public class ProdottoBean {
    private Integer id;
    private String nome;
    // Ora la categoria è memorizzata come String, per evitare riferimenti al model
    private String categoria;
    private BigDecimal prezzo;
    private Boolean disponibile;

    // Getter e Setter
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getCategoria() {
        return categoria;
    }
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    public BigDecimal getPrezzo() {
        return prezzo;
    }
    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }
    public Boolean getDisponibile() {
        return disponibile;
    }
    public void setDisponibile(Boolean disponibile) {
        this.disponibile = disponibile;
    }
}
