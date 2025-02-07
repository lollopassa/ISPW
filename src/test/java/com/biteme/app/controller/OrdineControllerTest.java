package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.model.Ordine;
import com.biteme.app.model.Prodotto;
import com.biteme.app.model.Categoria;
import com.biteme.app.persistence.inmemory.InMemoryOrdineDao;
import com.biteme.app.persistence.inmemory.InMemoryProdottoDao;
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
    private InMemoryOrdineDao inMemoryOrdineDao;
    private InMemoryProdottoDao inMemoryProdottoDao;

    @BeforeEach
    void setUp() throws Exception {
        controller = new OrdineController();
        inMemoryOrdineDao = new InMemoryOrdineDao();
        inMemoryProdottoDao = new InMemoryProdottoDao();

        // Injection dei DAO via reflection
        injectDao("ordineDao", inMemoryOrdineDao);
        injectDao("prodottoDao", inMemoryProdottoDao);
    }

    private void injectDao(String fieldName, Object dao) throws Exception {
        Field field = OrdineController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, dao);
    }

    @AfterEach
    void tearDown() {
        // Reset degli stati se necessario
    }

    @Test
    void testSalvaOrdine() {
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setProdotti(List.of("Pizza Margherita", "Coca Cola"));
        ordineBean.setQuantita(List.of(2, 1));

        controller.salvaOrdine(ordineBean, 1);

        Ordine ordine = inMemoryOrdineDao.getById(1);
        assertNotNull(ordine);
        assertEquals(1, ordine.getId());
        assertEquals(List.of("Pizza Margherita", "Coca Cola"), ordine.getProdotti());
        assertEquals(List.of(2, 1), ordine.getQuantita());
    }

    @Test
    void testGetOrdini() {
        inMemoryOrdineDao.store(new Ordine(1, List.of("Pizza Margherita"), List.of(2)));

        OrdineBean bean = controller.getOrdineById(1);
        assertNotNull(bean);
        assertEquals(1, bean.getId());
        assertEquals("Pizza Margherita", bean.getProdotti().get(0));
        assertEquals(2, bean.getQuantita().get(0));
    }

    @Test
    void testGetOrdineByIdNonEsistente() {
        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.getOrdineById(999)
        );
        assertEquals("Ordine con ID 999 non trovato", ex.getMessage());
    }

    @Test
    void testGetProdottiByCategoria() {
        inMemoryProdottoDao.store(new Prodotto(1, "Pizza Margherita", new BigDecimal("8.50"), Categoria.PIZZE, true));
        inMemoryProdottoDao.store(new Prodotto(2, "Pasta al Pomodoro", new BigDecimal("7.00"), Categoria.PRIMI, true));

        List<ProdottoBean> prodotti = controller.getProdottiByCategoria("PIZZE");
        assertEquals(1, prodotti.size());
        ProdottoBean bean = prodotti.get(0);
        assertEquals("Pizza Margherita", bean.getNome());
        assertEquals("8.50", bean.getPrezzo().toString());
    }

    @Test
    void testGetTuttiProdotti() {
        inMemoryProdottoDao.store(new Prodotto(1, "Pizza Margherita", new BigDecimal("8.50"), Categoria.PIZZE, true));
        inMemoryProdottoDao.store(new Prodotto(2, "Pasta al Pomodoro", new BigDecimal("7.00"), Categoria.PRIMI, true));

        List<ProdottoBean> prodotti = controller.getTuttiProdotti();
        assertEquals(2, prodotti.size());
    }
}