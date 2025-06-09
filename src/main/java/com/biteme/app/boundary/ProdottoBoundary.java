package com.biteme.app.boundary;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.LoginController;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.exception.ProdottoException;

import java.math.BigDecimal;
import java.util.List;


public class ProdottoBoundary {

    private final ProdottoController prodottoController;
    private final LoginController loginController;

    public ProdottoBoundary() {
        this.prodottoController = new ProdottoController();
        this.loginController = new LoginController();
    }


    public boolean isUserAdmin() {
        return loginController.isUserAdmin();
    }


    public List<ProdottoBean> getProdotti() {
        return prodottoController.getProdotti();
    }


    public void aggiungiProdotto(String nome,
                                 String categoria,
                                 BigDecimal prezzo) throws ProdottoException {
        ProdottoBean bean = new ProdottoBean();
        bean.setNome(nome);
        bean.setCategoria(categoria);
        bean.setPrezzo(prezzo);
        bean.setDisponibile(true);
        bean.validate();
        prodottoController.aggiungiProdotto(bean);
    }


    public void modificaProdotto(int id,
                                 String nome,
                                 String categoria,
                                 BigDecimal prezzo,
                                 boolean disponibile) throws ProdottoException {
        ProdottoBean bean = new ProdottoBean();
        bean.setId(id);
        bean.setNome(nome);
        bean.setCategoria(categoria);
        bean.setPrezzo(prezzo);
        bean.setDisponibile(disponibile);
        bean.validate();
        prodottoController.modificaProdotto(bean);
    }


    public void eliminaProdotto(int id) throws ProdottoException {
        prodottoController.eliminaProdotto(id);
    }
}