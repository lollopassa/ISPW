package com.biteme.app.boundary;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.GestioneOrdiniController;
import com.biteme.app.controller.ArchivioController;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.exception.ArchiviazioneException;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.exception.OrdineException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GestioneOrdiniBoundary {

    private final GestioneOrdiniController gestCtrl = new GestioneOrdiniController();
    private final ArchivioController       archivioCtrl = new ArchivioController();
    private final ProdottoController       prodottoCtrl  = new ProdottoController();

    public void createOrdinazione(String nome,
                                  String tipoOrdine,
                                  String orario,
                                  String nClienti,
                                  String infoTavolo) throws OrdinazioneException {
        OrdinazioneBean b = new OrdinazioneBean();
        b.setNome(nome);
        b.setTipoOrdine(tipoOrdine);
        b.setOrarioCreazione(orario);
        b.setNumeroClienti(nClienti);
        b.setInfoTavolo(infoTavolo);
        b.validate();
        gestCtrl.creaOrdinazione(b);
    }

    public List<OrdinazioneBean> getAll() {
        return gestCtrl.getOrdinazioni();
    }

    public void delete(int id) throws OrdinazioneException {
        gestCtrl.eliminaOrdinazione(id);
    }

    public void aggiornaStatoOrdinazione(int ordineId, StatoOrdinazione nuovo) throws OrdinazioneException {
        gestCtrl.aggiornaStatoOrdinazione(ordineId, nuovo);
    }

    public OrdineBean getOrdine(int ordineId) throws OrdineException {
        return gestCtrl.getOrdineById(ordineId);
    }

    public List<ProdottoBean> getProdottiByCategoria(String categoria) {
        return gestCtrl.getProdottiByCategoria(categoria);
    }

    public void salvaOrdineCompleto(int ordineId,
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
        gestCtrl.salvaOrdine(bean, ordineId);
    }

    public void archive(OrdinazioneBean ordBean) throws ArchiviazioneException {
        try {
            OrdineBean ordine = fetchOrdineOrStub(ordBean.getId());

            BigDecimal totale = calcolaTotaleOrdine(ordine);

            List<String> prodNames = ordine.getProdotti();
            List<Integer> qtys      = ordine.getQuantita();
            List<ProdottoBean> prodotti = new ArrayList<>(prodNames.size());
            List<Integer>      quantita = new ArrayList<>(qtys.size());

            for (int i = 0; i < prodNames.size(); i++) {
                String nomeProd = prodNames.get(i);
                BigDecimal prezzoDaOrdine =
                        (ordine.getPrezzi()!=null && ordine.getPrezzi().size()>i)
                                ? ordine.getPrezzi().get(i)
                                : BigDecimal.ZERO;

                ProdottoBean pb = prodottoCtrl.findProdottoSeEsiste(nomeProd);
                if (pb == null) {

                    pb = new ProdottoBean();
                    pb.setNome(nomeProd);
                    pb.setCategoria(null);
                    pb.setDisponibile(false);
                    pb.setPrezzo(prezzoDaOrdine);
                }
                prodotti.add(pb);
                quantita.add(qtys.get(i));
            }

            ArchivioBean av = new ArchivioBean();
            av.setIdOrdine(ordBean.getId() > 0 ? ordBean.getId() : null);
            av.setProdotti(prodotti);
            av.setQuantita(quantita);
            av.setTotale(totale);
            av.setDataArchiviazione(LocalDateTime.now());
            av.validate();

            archivioCtrl.archiviaOrdine(av);

            if (ordBean.getId() > 0) {
                gestCtrl.eliminaOrdinazione(ordBean.getId());
            }

        } catch (OrdinazioneException e) {
            throw new ArchiviazioneException(
                    "Errore durante l'archiviazione (ID ordine = " + ordBean.getId() + ")", e
            );
        }
    }

    private OrdineBean fetchOrdineOrStub(int id) {
        try {
            return gestCtrl.getOrdineById(id);
        } catch (OrdineException e) {
            OrdineBean stub = new OrdineBean();
            stub.setId(id);
            stub.setProdotti(List.of());
            stub.setQuantita(List.of());
            stub.setPrezzi(List.of());
            return stub;
        }
    }

    private BigDecimal calcolaTotaleOrdine(OrdineBean ob) {
        BigDecimal tot = BigDecimal.ZERO;
        List<String> prod   = ob.getProdotti();
        List<Integer> qty   = ob.getQuantita();
        List<BigDecimal> pr = ob.getPrezzi();
        for (int i = 0; i < prod.size(); i++) {
            BigDecimal prezzo = (pr != null && pr.size()>i) ? pr.get(i) : null;
            if (prezzo == null) {
                ProdottoBean pb = prodottoCtrl.findProdottoSeEsiste(prod.get(i));
                prezzo = (pb != null) ? pb.getPrezzo() : BigDecimal.ZERO;
            }
            tot = tot.add(prezzo.multiply(BigDecimal.valueOf(qty.get(i))));
        }
        return tot;
    }

    private static OrdinazioneBean selected;
    public static void setSelected(OrdinazioneBean o) { selected = o; }
    public static OrdinazioneBean getSelected() { return selected; }
}