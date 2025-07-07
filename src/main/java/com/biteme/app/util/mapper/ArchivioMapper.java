package com.biteme.app.util.mapper;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entities.Archivio;
import com.biteme.app.entities.ArchivioRiga;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.entities.Categoria;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ArchivioMapper implements BeanEntityMapper<ArchivioBean, Archivio> {

    @Override
    public Archivio toEntity(ArchivioBean bean) {
        bean.validate();

        List<ArchivioRiga> righe = new ArrayList<>();
        List<ProdottoBean> prodottoBeans = bean.getProdotti();
        List<Integer> quantitaList = bean.getQuantita();

        IntStream.range(0, prodottoBeans.size()).forEach(i -> {
            ProdottoBean pb = prodottoBeans.get(i);
            Integer qty = quantitaList.get(i);

            // converte ProdottoBean → Prodotto entity
            Prodotto prodEntity = toEntity(pb);
            righe.add(new ArchivioRiga(prodEntity, qty));
        });

        return new Archivio(
                bean.getIdOrdine(),
                righe,
                bean.getTotale(),
                bean.getDataArchiviazione()
        );
    }

    @Override
    public ArchivioBean toBean(Archivio entity) {
        ArchivioBean bean = new ArchivioBean();
        bean.setIdOrdine(entity.getIdOrdine());

        List<ProdottoBean> prodottoBeans = new ArrayList<>();
        List<Integer> quantitaList = new ArrayList<>();

        for (ArchivioRiga r : entity.getRighe()) {
            // converte Prodotto entity → ProdottoBean
            prodottoBeans.add(ProdottoBean.fromEntity(r.getProdotto()));
            quantitaList.add(r.getQuantita());
        }

        bean.setProdotti(prodottoBeans);
        bean.setQuantita(quantitaList);
        bean.setTotale(entity.getTotale());
        bean.setDataArchiviazione(entity.getDataArchiviazione());
        return bean;
    }

    // Helper per convertire ProdottoBean → Prodotto entity, gestendo correttamente Categoria
    private Prodotto toEntity(ProdottoBean pb) {
        Categoria cat = null;
        if (pb.getCategoria() != null) {
            // cerca l'enum il cui toString (displayName) corrisponde alla stringa nel bean
            for (Categoria c : Categoria.values()) {
                if (c.toString().equalsIgnoreCase(pb.getCategoria())) {
                    cat = c;
                    break;
                }
            }
        }
        return new Prodotto(
                pb.getId() != null ? pb.getId() : 0,
                pb.getNome(),
                pb.getPrezzo(),
                cat,
                Boolean.TRUE.equals(pb.getDisponibile())
        );
    }
}
