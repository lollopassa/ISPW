package com.biteme.app.bean;

import java.math.BigDecimal;
import com.biteme.app.exception.ProdottoException;

public class ProdottoBean {
    private Integer id;
    private String nome;
    private String categoria;
    private BigDecimal prezzo;
    private Boolean disponibile;

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

    public void validate() {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ProdottoException("Il nome del prodotto non puo' essere vuoto.");
        }
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new ProdottoException("Seleziona una categoria valida.");
        }
        if (prezzo == null || prezzo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProdottoException("Inserisci un valore numerico valido per il prezzo maggiore di zero.");
        }
    }
}
