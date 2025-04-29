package com.biteme.app.boundary;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.LoginController;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.exception.ProdottoException;

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


    public void aggiungiProdotto(ProdottoBean bean) throws ProdottoException {
        bean.validate();
        prodottoController.aggiungiProdotto(bean);
    }


    public void modificaProdotto(ProdottoBean bean) throws ProdottoException {
        bean.validate();
        prodottoController.modificaProdotto(bean);
    }


    public void eliminaProdotto(int id) throws ProdottoException {
        prodottoController.eliminaProdotto(id);
    }
}