package com.biteme.app.boundary;

import com.biteme.app.bean.*;
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
import java.util.ArrayList;
import java.util.List;

/** Facciata che orchestra l’intero flusso Ordine + Ordinazione + Archivio. */
public class OrdinazioneBoundary {

    private final OrdinazioneController ordinazioneCtrl = new OrdinazioneController();
    private final OrdineController      ordineCtrl      = new OrdineController();
    private final ArchivioController    archivioCtrl    = new ArchivioController();
    private final ProdottoController    prodottoCtrl    = new ProdottoController();

    /* -------- CRUD Ordinazione -------- */

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
        ordinazioneCtrl.creaOrdine(b);
    }

    public List<OrdinazioneBean> getAll() { return ordinazioneCtrl.getOrdini(); }

    public void delete(int id) throws OrdinazioneException { ordinazioneCtrl.eliminaOrdinazione(id); }

    public void aggiornaStatoOrdinazione(int ordineId, StatoOrdinazione nuovo)
            throws OrdinazioneException {
        ordinazioneCtrl.aggiornaStatoOrdinazione(ordineId, nuovo);
    }

    /* -------- ARCHIVIAZIONE -------- */

    /* ===== metodo pubblico ===== */
    public void archive(OrdinazioneBean ordBean) throws ArchiviazioneException {

        try {
            /* 1. recupero Ordine (o stub) */
            OrdineBean ordine = fetchOrdineOrStub(ordBean.getId());

            /* 2. totale */
            BigDecimal totale = calcolaTotaleOrdine(ordine);

            /* 3. righe archivio */
            List<ArchivioRigaBean> righe = new ArrayList<>();
            List<String>  prod   = ordine.getProdotti();
            List<Integer> quanti = ordine.getQuantita();

            for (int i = 0; i < prod.size(); i++) {
                BigDecimal prezzoDaOrdine = (ordine.getPrezzi()!=null && ordine.getPrezzi().size()>i)
                        ? ordine.getPrezzi().get(i)
                        : BigDecimal.ZERO;

                ArchivioRigaBean r = new ArchivioRigaBean();
                r.setProdottoBean(ensureProdottoBean(prod.get(i), prezzoDaOrdine));
                r.setQuantita(quanti.get(i));
                righe.add(r);
            }

            /* 4. ArchivioBean */
            ArchivioBean av = new ArchivioBean();
            int idOrd = ordBean.getId();
            av.setIdOrdine(idOrd > 0 ? idOrd : null);
            av.setRighe(righe);
            av.setTotale(totale);
            av.setDataArchiviazione(LocalDateTime.now());
            av.validate();

            /* 5. persistenza */
            archivioCtrl.archiviaOrdine(av);

            /* 6. cleanup ordinazione */
            if (idOrd > 0) {
                ordinazioneCtrl.eliminaOrdinazione(idOrd);
            }

        } catch (OrdineException | OrdinazioneException e) {
            throw new ArchiviazioneException(
                    "Errore durante l'archiviazione (ID ordine = " + ordBean.getId() + ")", e);
        }
    }

    /* ===== helper estratto ===== */
    private OrdineBean fetchOrdineOrStub(int ordineId) {
        try {
            return ordineCtrl.getOrdineById(ordineId);
        } catch (OrdineException e) {
            // nessun ordine salvato: movimento libero
            OrdineBean stub = new OrdineBean();
            stub.setId(ordineId);               // può essere 0
            stub.setProdotti(List.of());
            stub.setQuantita(List.of());
            stub.setPrezzi(List.of());
            return stub;
        }
    }

    private ProdottoBean ensureProdottoBean(String nome, BigDecimal prezzoDaOrdine) {
        ProdottoBean pb = prodottoCtrl.getProdottoByNome(nome);
        if (pb != null) return pb;                     // esiste in magazzino

        // ---- placeholder solo per l’archivio ----
        pb = new ProdottoBean();
        pb.setNome(nome);
        pb.setCategoria("Extra");                      // o null se preferisci
        pb.setPrezzo(prezzoDaOrdine != null ? prezzoDaOrdine : BigDecimal.ZERO);
        return pb;
    }



    /* -------- Helper -------- */

    private BigDecimal calcolaTotaleOrdine(OrdineBean ob) throws OrdineException {
        BigDecimal tot = BigDecimal.ZERO;

        List<String>  prodotti = ob.getProdotti();
        List<Integer> quanti   = ob.getQuantita();
        List<BigDecimal> prezzi = ob.getPrezzi(); // può essere null

        if (prodotti == null || quanti == null || prodotti.size() != quanti.size())
            throw new OrdineException("Prodotti e quantità non congruenti.");

        for (int i = 0; i < prodotti.size(); i++) {
            BigDecimal prezzo = (prezzi != null && prezzi.size() == prodotti.size())
                    ? prezzi.get(i)
                    : null;                               // non fornito dalla UI

            if (prezzo == null) {
                ProdottoBean pb = prodottoCtrl.getProdottoByNome(prodotti.get(i));
                if (pb == null)
                    throw new OrdineException("Prezzo mancante per \"" + prodotti.get(i) + "\"");
                prezzo = pb.getPrezzo();
            }
            tot = tot.add(prezzo.multiply(BigDecimal.valueOf(quanti.get(i))));
        }
        return tot;
    }


    /* -------- Selection state (UI helper) -------- */
    private static OrdinazioneBean selected;
    public static void   setSelected(OrdinazioneBean o) { selected = o; }
    public static OrdinazioneBean getSelected()         { return selected; }
}
