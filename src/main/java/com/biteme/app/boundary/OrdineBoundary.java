package com.biteme.app.boundary;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.exception.OrdinazioneException;

import java.math.BigDecimal;
import java.util.List;

/** Facciata lato UI per le operazioni sugli ordini. */
public class OrdineBoundary {

    private final OrdineController controller = new OrdineController();

    /* ======== LETTURA ======== */

    /** Versione richiesta dal nuovo OrdineUI */
    public OrdineBean getOrdine(int ordineId) throws OrdineException {
        return controller.getOrdineById(ordineId);
    }

    public List<ProdottoBean> getProdottiByCategoria(String categoria) {
        return controller.getProdottiByCategoria(categoria);
    }

    /* ======== SCRITTURA ======== */

    public void salvaOrdineCompleto(int ordineId,
                                    List<String>        prodotti,
                                    List<Integer>       quantita,
                                    List<BigDecimal>    prezzi)
            throws OrdineException {

        OrdineBean bean = new OrdineBean();
        bean.setId(ordineId);
        bean.setProdotti(prodotti);
        bean.setQuantita(quantita);
        bean.setPrezzi(prezzi);               // può essere null / vuota

        bean.validate();                      // coerenza lato DTO
        controller.salvaOrdine(bean, ordineId);
    }

    /* ======== OPERAZIONI SU ORDINAZIONE (deleghe) ======== */

    /** Permette a OrdineUI di cambiare lo stato dell’ordinazione
     *  collegata all’ordine appena “checkoutato”. */
    public void aggiornaStatoOrdinazione(int ordineId,
                                         StatoOrdinazione nuovoStato)
            throws OrdinazioneException {

        new OrdinazioneBoundary()
                .aggiornaStatoOrdinazione(ordineId, nuovoStato);
    }
}
