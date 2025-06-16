package com.biteme.app.boundary;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.exception.OrdineException;

import java.math.BigDecimal;
import java.util.List;

public class OrdineBoundary {

    private final OrdineController controller = new OrdineController();

    public OrdineBean loadOrdine(int ordineId) throws OrdineException {
        return controller.load(ordineId);
    }

    public List<ProdottoBean> getProdottiByCategoria(String categoria) {
        return controller.getProdottiByCategoria(categoria);
    }

    public void salvaOrdineCompleto(
            int ordineId,
            List<String> prodotti,
            List<Integer> quantita,
            List<BigDecimal> prezzi)
            throws OrdineException {

        OrdineBean bean = new OrdineBean();
        bean.setId(ordineId);
        bean.setProdotti(prodotti);
        bean.setQuantita(quantita);
        bean.setPrezzi(prezzi);

        bean.validate();

        controller.salvaOrdine(bean, ordineId);
    }

}