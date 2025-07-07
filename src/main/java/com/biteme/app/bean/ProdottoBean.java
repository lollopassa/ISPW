package com.biteme.app.bean;

import java.math.BigDecimal;

import com.biteme.app.entities.Prodotto;
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
            throw new ProdottoException("Il nome del prodotto non può essere vuoto.");
        }

        /* vincoli solo per prodotti a catalogo */
        if (Boolean.TRUE.equals(disponibile)) {

            if (categoria == null || categoria.trim().isEmpty()) {
                throw new ProdottoException("Seleziona una categoria valida.");
            }
            if (prezzo == null || prezzo.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ProdottoException("Il prezzo deve essere maggiore di zero.");
            }

        } else {
            if (prezzo == null) prezzo = BigDecimal.ZERO;
        }
    }



    public static ProdottoBean fromEntity(Prodotto p) {
        ProdottoBean b = new ProdottoBean();
        b.setId(p.getId());
        b.setNome(p.getNome());
        b.setPrezzo(p.getPrezzo());
        b.setCategoria(p.getCategoria().name());
        b.setDisponibile(p.isDisponibile());
        return b;
    }

}
