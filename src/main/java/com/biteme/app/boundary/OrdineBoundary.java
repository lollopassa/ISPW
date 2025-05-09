package com.biteme.app.boundary;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.exception.OrdineException;
import javafx.scene.layout.VBox;

import java.util.List;

public class OrdineBoundary {

    private final OrdineController controller = new OrdineController();

    public void setRiepilogoContent(VBox vbox) {
        controller.setRiepilogoContenuto(vbox);
    }

    public OrdineBean loadOrdine(int ordineId) throws OrdineException {
        return controller.load(ordineId);
    }

    public List<ProdottoBean> getProdottiByCategoria(String categoria) {
        return controller.getProdottiByCategoria(categoria);
    }

    public void salvaOrdineCompleto(int ordineId, OrdineBean bean) throws OrdineException {
        controller.salvaOrdine(bean, ordineId);
    }

    public List<ProdottoBean> getTuttiProdotti() {
        return controller.getTuttiProdotti();
    }

    public List<String> recuperaProdottiDalRiepilogo() {
        return controller.recuperaProdottiDalRiepilogo();
    }

    public int recuperaQuantitaDalRiepilogo(String nomeProdotto) {
        return controller.recuperaQuantitaDalRiepilogo(nomeProdotto);
    }
}