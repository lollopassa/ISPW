package com.biteme.app.util.mapper;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entities.Prodotto;

public class ProdottoMapper implements BeanEntityMapper<ProdottoBean, Prodotto> {

    @Override
    public Prodotto toEntity(ProdottoBean bean) {
        bean.validate();
        return new Prodotto(
                bean.getId() != null ? bean.getId() : 0,
                bean.getNome(),
                bean.getPrezzo(),
                com.biteme.app.entities.Categoria.valueOf(bean.getCategoria().toUpperCase()),
                bean.getDisponibile() != null && bean.getDisponibile()
        );
    }

    @Override
    public ProdottoBean toBean(Prodotto entity) {
        ProdottoBean bean = new ProdottoBean();
        bean.setId(entity.getId());
        bean.setNome(entity.getNome());
        bean.setPrezzo(entity.getPrezzo());
        bean.setCategoria(entity.getCategoria().name());
        bean.setDisponibile(entity.isDisponibile());
        return bean;
    }
}