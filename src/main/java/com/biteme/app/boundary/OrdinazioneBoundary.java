package com.biteme.app.boundary;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.ArchivioController;
import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.exception.ArchiviazioneException;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.exception.OrdineException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrdinazioneBoundary {

    private final OrdinazioneController ordinazioneController = new OrdinazioneController();
    private final OrdineController ordineController         = new OrdineController();
    private final ArchivioController archivioController       = new ArchivioController();
    private final ProdottoController prodottoController = new ProdottoController(); // Aggiungi questo


    private static OrdinazioneBean selected;

    public OrdinazioneBean createOrdinazione(String nome, String tipoOrdine, String orarioCreazione,
                                  String numeroClienti, String infoTavolo) throws OrdinazioneException {
        OrdinazioneBean bean = new OrdinazioneBean();
        bean.setNome(nome);
        bean.setTipoOrdine(tipoOrdine);
        bean.setOrarioCreazione(orarioCreazione);
        bean.setNumeroClienti(numeroClienti);
        bean.setInfoTavolo(infoTavolo);
        bean.validate();
        ordinazioneController.creaOrdine(bean);
        return bean;
    }

    public List<OrdinazioneBean> getAll() {
        return ordinazioneController.getOrdini();
    }

    public void delete(int id) throws OrdinazioneException {
        ordinazioneController.eliminaOrdinazione(id);
    }

    public void aggiornaStatoOrdinazione(int ordineId, StatoOrdinazione nuovoStato)
            throws OrdinazioneException {
        ordinazioneController.aggiornaStatoOrdinazione(ordineId, nuovoStato);
    }

    public void archive(OrdinazioneBean bean) throws ArchiviazioneException {
        try {
            OrdineBean ordine = ordineController.load(bean.getId());

            BigDecimal totale = calcolaTotaleOrdine(ordine);

            ArchivioBean av = new ArchivioBean();
            av.setIdOrdine(ordine.getId());
            av.setProdotti(ordine.getProdotti());
            av.setQuantita(ordine.getQuantita());
            av.setTotale(totale);
            av.setDataArchiviazione(LocalDateTime.now());
            av.validate();

            archivioController.archiviaOrdine(av);
            ordinazioneController.eliminaOrdinazione(bean.getId());

        } catch (OrdineException | OrdinazioneException e) {
            throw new ArchiviazioneException(
                    "Errore durante l'archiviazione dell'ordine ID=" + bean.getId(), e);
        }
    }

    private BigDecimal calcolaTotaleOrdine(OrdineBean ordineBean) throws OrdineException {
        BigDecimal totale = BigDecimal.ZERO;
        List<String> prodotti = ordineBean.getProdotti();
        List<Integer> quantita = ordineBean.getQuantita();
        List<BigDecimal> prezziBean = ordineBean.getPrezzi();

        if (prezziBean != null
                && prodotti != null
                && quantita != null
                && prodotti.size() == quantita.size()
                && prezziBean.size() == prodotti.size())
        {
            for (int i = 0; i < prodotti.size(); i++) {
                totale = totale.add(
                        prezziBean.get(i)
                                .multiply(BigDecimal.valueOf(quantita.get(i)))
                );
            }
            return totale;
        }

        if (prodotti != null && quantita != null && prodotti.size() == quantita.size()) {
            for (int i = 0; i < prodotti.size(); i++) {
                ProdottoBean prodotto = prodottoController.getProdottoByNome(prodotti.get(i));
                if (prodotto == null) {
                    throw new OrdineException("Prodotto non trovato: " + prodotti.get(i));
                }
                totale = totale.add(
                        prodotto.getPrezzo().multiply(BigDecimal.valueOf(quantita.get(i)))
                );
            }
            return totale;
        }

        throw new OrdineException("Dati ordine non validi per il calcolo del totale.");
    }

    public static void setSelected(OrdinazioneBean o) { selected = o; }
    public static OrdinazioneBean getSelected()   { return selected; }
}