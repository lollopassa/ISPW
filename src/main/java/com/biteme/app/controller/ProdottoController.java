package com.biteme.app.controller;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.exception.ProdottoException;
import com.biteme.app.model.Prodotto;
import com.biteme.app.model.Categoria;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.util.Configuration;
import javafx.scene.control.Alert;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

public class ProdottoController {

    private final ProdottoDao prodottoDao;

    public ProdottoController() {
        this.prodottoDao = Configuration.getPersistenceProvider().getDaoFactory().getProdottoDao();
    }

    // --- Utility methods for alerts ---
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setContentText(message);
        alert.showAndWait();
    }
    // --------------------------------

    public void aggiungiProdotto(ProdottoBean prodottoBean) {
        try {
            if (prodottoBean.getNome() == null || prodottoBean.getNome().isBlank()) {
                throw new ProdottoException("Il nome del prodotto non può essere vuoto.");
            }
            if (prodottoBean.getCategoria() == null || prodottoBean.getCategoria().isBlank()) {
                throw new ProdottoException("Seleziona una categoria valida.");
            }
            if (prodottoBean.getPrezzo() == null || prodottoBean.getPrezzo().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ProdottoException("Inserisci un valore numerico valido per il prezzo maggiore di zero.");
            }

            Prodotto prodotto = new Prodotto(
                    prodottoBean.getId() != null ? prodottoBean.getId() : 0,
                    prodottoBean.getNome(),
                    prodottoBean.getPrezzo(),
                    Categoria.valueOf(prodottoBean.getCategoria().toUpperCase()),
                    prodottoBean.getDisponibile() != null && prodottoBean.getDisponibile()
            );
            prodottoDao.store(prodotto);
            showInfo("Prodotto aggiunto correttamente!");
        } catch (Exception e) {
            showError("Errore nell'aggiunta del prodotto: " + e.getMessage());
        }
    }

    public void modificaProdotto(ProdottoBean prodottoBean) {
        try {
            if (prodottoBean.getId() == null || prodottoBean.getId() <= 0) {
                throw new ProdottoException("L'ID del prodotto non è valido.");
            }
            // Validate fields
            if (prodottoBean.getNome() == null || prodottoBean.getNome().isBlank()) {
                throw new ProdottoException("Il nome del prodotto non può essere vuoto.");
            }
            if (prodottoBean.getCategoria() == null || prodottoBean.getCategoria().isBlank()) {
                throw new ProdottoException("Seleziona una categoria valida.");
            }
            if (prodottoBean.getPrezzo() == null || prodottoBean.getPrezzo().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ProdottoException("Inserisci un valore numerico valido per il prezzo maggiore di zero.");
            }

            Prodotto prodottoAggiornato = new Prodotto(
                    prodottoBean.getId(),
                    prodottoBean.getNome(),
                    prodottoBean.getPrezzo(),
                    Categoria.valueOf(prodottoBean.getCategoria().toUpperCase()),
                    prodottoBean.getDisponibile() != null && prodottoBean.getDisponibile()
            );
            prodottoDao.update(prodottoAggiornato);
            showInfo("Prodotto aggiornato con successo!");
        } catch (Exception e) {
            showError("Errore nella modifica del prodotto: " + e.getMessage());
        }
    }

    public void eliminaProdotto(Integer id) {
        try {
            if (id == null || id <= 0) {
                throw new ProdottoException("L'ID del prodotto da eliminare non è valido.");
            }
            prodottoDao.delete(id);
            showInfo("Prodotto eliminato con successo!");
        } catch (Exception e) {
            showError("Errore nell'eliminazione del prodotto: " + e.getMessage());
        }
    }

    public List<ProdottoBean> getProdotti() {
        List<Prodotto> prodotti = prodottoDao.getByDisponibilita(true);
        return prodotti.stream()
                .map(this::mapProdottoToBean)
                .toList();
    }

    public ProdottoBean getProdottoByNome(String nome) {
        try {
            if (nome == null || nome.isBlank()) {
                throw new ProdottoException("Il nome del prodotto non può essere vuoto o nullo.");
            }
            Prodotto prodotto = prodottoDao.findByNome(nome);
            if (prodotto == null) {
                throw new ProdottoException("Prodotto con nome '" + nome + "' non trovato!");
            }
            return mapProdottoToBean(prodotto);
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).severe("Errore Prodotto: " + e.getMessage());
            showError("Errore nel recupero del prodotto: " + e.getMessage());
            return null;
        }
    }

    private ProdottoBean mapProdottoToBean(Prodotto prodotto) {
        ProdottoBean bean = new ProdottoBean();
        bean.setId(prodotto.getId());
        bean.setNome(prodotto.getNome());
        bean.setPrezzo(prodotto.getPrezzo());
        bean.setCategoria(prodotto.getCategoria().name());
        bean.setDisponibile(prodotto.isDisponibile());
        return bean;
    }
}
