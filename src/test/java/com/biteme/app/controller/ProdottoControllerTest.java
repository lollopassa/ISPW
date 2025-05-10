package com.biteme.app.controller;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.exception.ProdottoException;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.entities.Categoria;
import com.biteme.app.persistence.ProdottoDao;
import com.biteme.app.persistence.inmemory.Storage;
import com.biteme.app.persistence.Configuration;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

//@author Kevin Hoxha

class ProdottoControllerTest {

    private ProdottoController controller;
    private ProdottoDao prodottoDao;

    private final List<Integer> createdProdottoIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {

        controller = new ProdottoController();


        prodottoDao = Configuration.getPersistenceProvider().getDaoFactory().getProdottoDao();


        Field daoField = ProdottoController.class.getDeclaredField("prodottoDao");
        daoField.setAccessible(true);
        daoField.set(controller, prodottoDao);
    }

    @AfterEach
    void tearDown() {

        if (Configuration.getPersistenceProvider().getDaoFactory()
                instanceof com.biteme.app.persistence.inmemory.InMemoryDaoFactory) {
            Storage.getInstance().getProdotti().clear();
        } else {

            for (Integer id : createdProdottoIds) {
                if (prodottoDao.exists(id)) {
                    prodottoDao.delete(id);
                }
            }
        }
        createdProdottoIds.clear();
    }

    @Test
    void testAggiungiProdotto() {

        ProdottoBean bean = preparaProdottoBean("Pasta", "PRIMI", new BigDecimal("12"), true);


        controller.aggiungiProdotto(bean);


        ProdottoBean result = controller.getProdottoByNome("Pasta");
        assertNotNull(result, "Il prodotto non deve essere nullo.");
        createdProdottoIds.add(result.getId());


        assertTrue(result.getId() > 0);
        assertEquals("Pasta", result.getNome());

        assertEquals(0, result.getPrezzo().compareTo(new BigDecimal("12")));
        assertEquals("PRIMI", result.getCategoria());
        assertTrue(result.getDisponibile());
    }

    @Test
    void testModificaProdotto() {

        Prodotto prodotto = new Prodotto(0, "Fiorentina", new BigDecimal("75.00"), Categoria.SECONDI, true);
        prodottoDao.store(prodotto);
        int prodId = prodotto.getId();
        createdProdottoIds.add(prodId);


        ProdottoBean bean = preparaProdottoBean(prodId, "Bistecca alla Fiorentina", "SECONDI", new BigDecimal("95.00"), false);
        controller.modificaProdotto(bean);


        Optional<Prodotto> updatedProdotto = prodottoDao.load(prodId);
        assertTrue(updatedProdotto.isPresent(), "Il prodotto aggiornato deve essere presente.");
        Prodotto p = updatedProdotto.get();
        assertEquals("Bistecca alla Fiorentina", p.getNome());
        assertEquals(0, p.getPrezzo().compareTo(new BigDecimal("95.00")));
        assertEquals(Categoria.SECONDI, p.getCategoria());
        assertFalse(p.isDisponibile());
    }

    @Test
    void testEliminaProdotto() {

        Prodotto prodotto = new Prodotto(0, "Brodo di Carne", new BigDecimal("18.30"), Categoria.PRIMI, true);
        prodottoDao.store(prodotto);
        int prodId = prodotto.getId();
        createdProdottoIds.add(prodId);


        controller.eliminaProdotto(prodId);

                assertFalse(prodottoDao.exists(prodId));
        createdProdottoIds.remove(Integer.valueOf(prodId));
    }

    @Test
    void testGetProdotti() {
                Prodotto p1 = new Prodotto(0, "Pizza Margherita", new BigDecimal("5.50"), Categoria.PIZZE, true);
        Prodotto p2 = new Prodotto(0, "Tiramisu", new BigDecimal("8.50"), Categoria.DOLCI, false);
        Prodotto p3 = new Prodotto(0, "Insalata Mista", new BigDecimal("6.00"), Categoria.ANTIPASTI, true);

        prodottoDao.store(p1);
        prodottoDao.store(p2);
        prodottoDao.store(p3);
        createdProdottoIds.add(p1.getId());
        createdProdottoIds.add(p2.getId());
        createdProdottoIds.add(p3.getId());

                List<ProdottoBean> prodottiDisponibili = controller.getProdotti();
                List<ProdottoBean> createdProducts = prodottiDisponibili.stream()
                .filter(bean -> createdProdottoIds.contains(bean.getId()))
                .toList();
                assertEquals(2, createdProducts.size(), "I prodotti creati e disponibili devono essere 2.");
        for (ProdottoBean bean : createdProducts) {
            assertTrue(bean.getDisponibile());
        }
    }

    @Test
    void testGetProdottoByNome() {
                Prodotto prodotto = new Prodotto(0, "Gattò", new BigDecimal("15.00"), Categoria.SECONDI, true);
        prodottoDao.store(prodotto);
        int prodId = prodotto.getId();
        createdProdottoIds.add(prodId);

                ProdottoBean bean = controller.getProdottoByNome("Gattò");
        assertNotNull(bean);
        assertEquals("Gattò", bean.getNome());
        assertEquals(0, bean.getPrezzo().compareTo(new BigDecimal("15.00")));
        assertEquals("SECONDI", bean.getCategoria());
        assertTrue(bean.getDisponibile());
    }

    @Test
    void testGetProdottoByNomeNonTrovato() {
        Exception exception = assertThrows(ProdottoException.class, () -> controller.getProdottoByNome("NonEsistente"));
        assertTrue(exception.getMessage().contains("Prodotto con nome"));
    }

    @Test
    void testEliminaProdottoConIdNonValido() {
        Exception exception = assertThrows(ProdottoException.class, () -> controller.eliminaProdotto(null));
        assertEquals("L'ID del prodotto da eliminare non è valido.", exception.getMessage());
    }


    private ProdottoBean preparaProdottoBean(String nome, String categoria, BigDecimal prezzo, boolean disponibile) {
        return preparaProdottoBean(0, nome, categoria, prezzo, disponibile);
    }

    private ProdottoBean preparaProdottoBean(Integer id, String nome, String categoria, BigDecimal prezzo, boolean disponibile) {
        ProdottoBean bean = new ProdottoBean();
        bean.setId(id);
        bean.setNome(nome);
        bean.setCategoria(categoria);
        bean.setPrezzo(prezzo);
        bean.setDisponibile(disponibile);
        return bean;
    }
}