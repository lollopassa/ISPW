package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entities.Ordine;
import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.entities.Categoria;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.entities.TipoOrdinazione;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.persistence.OrdineDao;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.persistence.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@author Lorenzo Passacantilli

class OrdineControllerTest {

    private OrdineController controller;
    private OrdineDao ordineDao;
    private ProdottoDao prodottoDao;
    private OrdinazioneDao ordinazioneDao;

    private int parentId;

    @BeforeEach
    void setUp() throws Exception {
        controller = new OrdineController();

        ordineDao     = Configuration.getPersistenceProvider().getDaoFactory().getOrdineDao();
        prodottoDao   = Configuration.getPersistenceProvider().getDaoFactory().getProdottoDao();
        ordinazioneDao= Configuration.getPersistenceProvider().getDaoFactory().getOrdinazioneDao();

        injectDao("ordineDao",    ordineDao);
        injectDao("prodottoDao",  prodottoDao);

        Ordinazione dummy = new Ordinazione(
                0,
                "Alberto Verdi",
                "1",
                TipoOrdinazione.ASPORTO,
                "None",
                StatoOrdinazione.NUOVO,
                "12:00"
        );
        ordinazioneDao.store(dummy);
        parentId = dummy.getId();

        removeProdottoIfExists("Pizza Margherita");
        removeProdottoIfExists("Pasta al Pomodoro");
    }

    @AfterEach
    void tearDown() {
        if (ordineDao.exists(parentId)) {
            ordineDao.delete(parentId);
        }
        if (ordinazioneDao.exists(parentId)) {
            ordinazioneDao.delete(parentId);
        }
        removeProdottoIfExists("Pizza Margherita");
        removeProdottoIfExists("Pasta al Pomodoro");
    }

    private void injectDao(String fieldName, Object dao) throws Exception {
        Field field = OrdineController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, dao);
    }

    private void removeProdottoIfExists(String nome) {
        prodottoDao.getAll().stream()
                .filter(p -> p.getNome().equals(nome))
                .forEach(p -> prodottoDao.delete(p.getId()));
    }

    @Test
    void testSalvaOrdine() throws OrdineException {
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setProdotti(List.of("Pizza Margherita", "Coca Cola"));
        ordineBean.setQuantita(List.of(2, 1));


        ordineBean.setPrezzi(List.of(
                new BigDecimal("8.50"),
                new BigDecimal("3.00")
        ));

        controller.salvaOrdine(ordineBean, parentId);

        Ordine ordine = ordineDao.getById(parentId);
        assertNotNull(ordine, "L'ordine non dovrebbe essere null");
        assertEquals(parentId, ordine.getId(),
                "L'ID dell'ordine deve essere uguale all'id del record padre");
        assertEquals(List.of("Pizza Margherita", "Coca Cola"),
                ordine.getProdotti(),  "I prodotti non coincidono");
        assertEquals(List.of(2, 1),
                ordine.getQuantita(),  "Le quantitÃ  non coincidono");

        assertEquals(List.of(new BigDecimal("8.50"), new BigDecimal("3.00")),
                ordine.getPrezzi(), "I prezzi non coincidono");
    }


    @Test
    void testGetOrdineByIdEsistente() throws OrdineException {
        Ordine ordineDaSalvare = new Ordine(parentId,
                List.of("Pizza Margherita"),
                List.of(2),
                List.of(new BigDecimal("8.50")));
        ordineDao.store(ordineDaSalvare);
        int storedId = ordineDaSalvare.getId();

        OrdineBean bean = controller.getOrdineById(storedId);
        assertNotNull(bean, "L'ordine deve essere presente.");
        assertEquals(storedId, bean.getId());
        assertEquals("Pizza Margherita", bean.getProdotti().get(0));
        assertEquals(2, bean.getQuantita().get(0));
    }


    @Test
    void testGetProdottiByCategoria() {
        Prodotto p1 = new Prodotto(0, "Pizza Margherita", new BigDecimal("8.50"),
                Categoria.PIZZE, true);
        Prodotto p2 = new Prodotto(0, "Pasta al Pomodoro", new BigDecimal("7.00"),
                Categoria.PRIMI, true);
        prodottoDao.store(p1);
        prodottoDao.store(p2);

        List<ProdottoBean> prodotti = controller.getProdottiByCategoria("PIZZE");
        var filtered = prodotti.stream()
                .filter(b -> "Pizza Margherita".equals(b.getNome()))
                .toList();
        assertEquals(1, filtered.size(),
                "Dovrebbe esserci un solo prodotto nella categoria PIZZE.");
        ProdottoBean bean = filtered.get(0);
        assertEquals("Pizza Margherita", bean.getNome());
        assertEquals("8.50", bean.getPrezzo().toString());
    }

    @Test
    void testGetTuttiProdotti() {
        Prodotto p1 = new Prodotto(0, "Pizza Margherita", new BigDecimal("8.50"),
                Categoria.PIZZE, true);
        Prodotto p2 = new Prodotto(0, "Pasta al Pomodoro", new BigDecimal("7.00"),
                Categoria.PRIMI, true);
        prodottoDao.store(p1);
        prodottoDao.store(p2);

        List<ProdottoBean> tutti = controller.getTuttiProdotti();
        var filtered = tutti.stream()
                .filter(b -> b.getNome().equals("Pizza Margherita")
                        || b.getNome().equals("Pasta al Pomodoro"))
                .toList();
        assertEquals(2, filtered.size(),
                "Dovrebbero essere presenti 2 prodotti corrispondenti.");
    }
}