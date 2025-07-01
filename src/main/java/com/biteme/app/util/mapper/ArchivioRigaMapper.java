package com.biteme.app.util.mapper;

import com.biteme.app.bean.ArchivioRigaBean;
import com.biteme.app.entities.ArchivioRiga;
import com.biteme.app.entities.Prodotto;

public class ArchivioRigaMapper implements BeanEntityMapper<ArchivioRigaBean, ArchivioRiga> {

    private final ProdottoMapper prodottoMapper = new ProdottoMapper();

    @Override
    public ArchivioRiga toEntity(ArchivioRigaBean bean) {
        Prodotto prodotto = prodottoMapper.toEntity(bean.getProdottoBean());
        return new ArchivioRiga(prodotto, bean.getQuantita());
    }

    @Override
    public ArchivioRigaBean toBean(ArchivioRiga entity) {
        ArchivioRigaBean b = new ArchivioRigaBean();
        b.setProdottoBean(prodottoMapper.toBean(entity.getProdotto()));
        b.setQuantita(entity.getQuantita());
        return b;
    }
}
