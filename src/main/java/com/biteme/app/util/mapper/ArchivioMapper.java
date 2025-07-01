package com.biteme.app.util.mapper;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.entities.Archivio;
import com.biteme.app.entities.ArchivioRiga;

import java.util.List;

public class ArchivioMapper implements BeanEntityMapper<ArchivioBean, Archivio> {

    private final ArchivioRigaMapper rigaMapper = new ArchivioRigaMapper();

    @Override
    public Archivio toEntity(ArchivioBean bean) {
        bean.validate();
        List<ArchivioRiga> righe = bean.getRighe().stream()
                .map(rigaMapper::toEntity)
                .toList();
        return new Archivio(
                bean.getIdOrdine(),
                righe,
                bean.getTotale(),
                bean.getDataArchiviazione()
        );
    }

    @Override
    public ArchivioBean toBean(Archivio entity) {
        ArchivioBean b = new ArchivioBean();
        b.setIdOrdine(entity.getIdOrdine());
        b.setRighe(entity.getRighe().stream()
                .map(rigaMapper::toBean)
                .toList());
        b.setTotale(entity.getTotale());
        b.setDataArchiviazione(entity.getDataArchiviazione());
        return b;
    }
}
