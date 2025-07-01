package com.biteme.app.util.mapper;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.entities.Ordine;
import com.biteme.app.exception.OrdineException;

public class OrdineMapper implements BeanEntityMapper<OrdineBean, Ordine> {

    @Override
    public Ordine toEntity(OrdineBean bean) {
        try {
            bean.validate();
        } catch (OrdineException ex) {
            throw new IllegalArgumentException("Ordine non valido: " + ex.getMessage(), ex);
        }
        return new Ordine(
                bean.getId(),
                bean.getProdotti(),
                bean.getQuantita(),
                bean.getPrezzi()
        );
    }

    @Override
    public OrdineBean toBean(Ordine entity) {
        OrdineBean b = new OrdineBean();
        b.setId(entity.getId());
        b.setProdotti(entity.getProdotti());
        b.setQuantita(entity.getQuantita());
        b.setPrezzi(entity.getPrezzi());
        return b;
    }
}
