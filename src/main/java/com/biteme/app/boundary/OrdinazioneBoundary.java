package com.biteme.app.boundary;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.bean.OrdineBean;
import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.controller.ArchivioController;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.exception.OrdineException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Boundary per Ordinazione: logica di business senza UI.
 */
public class OrdinazioneBoundary {
    private final OrdinazioneController ordinazioneController;
    private final OrdineController ordineController;
    private final ArchivioController archivioController;
    private static OrdinazioneBean ordineSelezionato;

    public OrdinazioneBoundary() {
        this.ordinazioneController = new OrdinazioneController();
        this.ordineController = new OrdineController();
        this.archivioController = new ArchivioController();
    }

    public OrdinazioneBean createOrdine(String nomeCliente,
                                        String tipoOrdine,
                                        String orario,
                                        String coperti,
                                        String tavolo) throws OrdinazioneException {
        OrdinazioneBean bean = ordinazioneController.processOrdineCreation(
                nomeCliente, tipoOrdine, orario, coperti, tavolo);
        bean.validate();
        ordinazioneController.creaOrdine(bean);
        return bean;
    }

    public void eliminaOrdine(int id) throws OrdinazioneException {
        ordinazioneController.eliminaOrdinazione(id);
    }

    public void archiviaOrdine(OrdinazioneBean sel) throws OrdinazioneException, OrdineException {
        OrdineBean ordineBean = ordineController.getOrdineById(sel.getId());
        BigDecimal totale = calcolaTotaleOrdine(ordineBean);

        ArchivioBean archivioBean = new ArchivioBean();
        archivioBean.setIdOrdine(ordineBean.getId());
        archivioBean.setProdotti(ordineBean.getProdotti());
        archivioBean.setQuantita(ordineBean.getQuantita());
        archivioBean.setTotale(totale);
        archivioBean.setDataArchiviazione(LocalDateTime.now());
        archivioBean.validate();

        archivioController.archiviaOrdine(archivioBean);
        ordinazioneController.eliminaOrdinazione(sel.getId());
    }

    public List<OrdinazioneBean> getOrdini() {
        return ordinazioneController.getOrdini();
    }

    public static void setOrdineSelezionato(OrdinazioneBean bean) {
        ordineSelezionato = bean;
    }

    public static OrdinazioneBean getOrdineSelezionato() {
        return ordineSelezionato;
    }

    private BigDecimal calcolaTotaleOrdine(OrdineBean ordineBean) {
        BigDecimal totale = BigDecimal.ZERO;
        List<String> prodotti = ordineBean.getProdotti();
        List<Integer> quantita = ordineBean.getQuantita();
        if (prodotti != null && quantita != null && prodotti.size() == quantita.size()) {
            for (int i = 0; i < prodotti.size(); i++) {
                BigDecimal prezzoUnitario = BigDecimal.valueOf(10);
                totale = totale.add(prezzoUnitario.multiply(BigDecimal.valueOf(quantita.get(i))));
            }
        }
        return totale;
    }
}