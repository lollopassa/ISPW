package com.biteme.app.controller;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.exception.ProdottoException;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.util.mapper.BeanEntityMapperFactory;

import java.util.List;

public class ProdottoController {

    private final ProdottoDao prodottoDao;
    private final BeanEntityMapperFactory mapperFactory = BeanEntityMapperFactory.getInstance();

    public ProdottoController() {
        this.prodottoDao = Configuration
                .getPersistenceProvider()
                .getDaoFactory()
                .getProdottoDao();
    }

    public void aggiungiProdotto(ProdottoBean bean) {
        Prodotto entity = mapperFactory.toEntity(bean, ProdottoBean.class);
        prodottoDao.create(entity);
        bean.setId(entity.getId());
    }

    public void modificaProdotto(ProdottoBean bean) {
        if (bean.getId() == null || bean.getId() <= 0) {
            throw new ProdottoException("L'ID del prodotto non è valido.");
        }
        Prodotto entity = mapperFactory.toEntity(bean, ProdottoBean.class);
        prodottoDao.update(entity);
    }

    public void eliminaProdotto(Integer id) {
        if (id == null || id <= 0) {
            throw new ProdottoException("L'ID del prodotto da eliminare non è valido.");
        }
        prodottoDao.delete(id);
    }

    public List<ProdottoBean> getProdotti() {
        List<Prodotto> entities = prodottoDao.getByDisponibilita(true);
        return entities.stream()
                .map(e -> mapperFactory.toBean(e, ProdottoBean.class))
                .toList();
    }

    public ProdottoBean getProdottoByNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ProdottoException("Il nome del prodotto non può essere vuoto o nullo.");
        }
        Prodotto entity = prodottoDao.findByNome(nome);
        if (entity == null) {
            throw new ProdottoException("Prodotto con nome '" + nome + "' non trovato!");
        }
        return mapperFactory.toBean(entity, ProdottoBean.class);
    }

    public ProdottoBean findProdottoSeEsiste(String nome) {
        if (nome == null || nome.isBlank()) return null;
        Prodotto entity = prodottoDao.findByNome(nome);
        return (entity != null) ? mapperFactory.toBean(entity, ProdottoBean.class)
                : null;
    }

}