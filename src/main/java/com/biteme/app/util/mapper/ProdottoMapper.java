package com.biteme.app.util.mapper;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entities.Categoria;
import com.biteme.app.entities.Prodotto;


public class ProdottoMapper implements BeanEntityMapper<ProdottoBean, Prodotto> {

    @Override
    public Prodotto toEntity(ProdottoBean bean) {
        /* 1. Validazione di base (consente categoria vuota se placeholder) */
        bean.validate();

        /* 2. Categoria “safe”: fallback a EXTRA se nulla o non riconosciuta */
        Categoria categoria;
        String catStr = bean.getCategoria();
        if (catStr == null || catStr.isBlank()) {
            categoria = Categoria.EXTRA;                               // ← fallback
        } else {
            try {
                categoria = Categoria.valueOf(catStr.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                categoria = Categoria.EXTRA;                           // ← fallback
            }
        }

        /* 3. Costruzione dell’entità persistente */
        return new Prodotto(
                bean.getId() != null ? bean.getId() : 0,
                bean.getNome(),
                bean.getPrezzo(),
                categoria,
                Boolean.TRUE.equals(bean.getDisponibile())             // null → false
        );
    }

    @Override
    public ProdottoBean toBean(Prodotto entity) {
        ProdottoBean bean = new ProdottoBean();
        bean.setId(entity.getId());
        bean.setNome(entity.getNome());
        bean.setPrezzo(entity.getPrezzo());
        bean.setCategoria(
                entity.getCategoria() != null ? entity.getCategoria().name() : null);
        bean.setDisponibile(entity.isDisponibile());
        return bean;
    }
}
