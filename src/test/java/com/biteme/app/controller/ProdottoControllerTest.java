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
    // Lista per tracciare gli ID dei prodotti creati durante i test
    private final List<Integer> createdProdottoIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        // Inizializza il controller
        controller = new ProdottoController();

        // Recupera il DAO generico per i prodotti tramite la configurazione
        prodottoDao = Configuration.getPersistenceProvider().getDaoFactory().getProdottoDao();

        // Usa reflection per iniettare il DAO nella classe ProdottoController
        Field daoField = ProdottoController.class.getDeclaredField("prodottoDao");
        daoField.setAccessible(true);
        daoField.set(controller, prodottoDao);
    }

    @AfterEach
    void tearDown() {
        // Se la persistenza è in memory, pulisci lo storage per garantire test isolati.
        if (Configuration.getPersistenceProvider().getDaoFactory()
                instanceof com.biteme.app.persistence.inmemory.InMemoryDaoFactory) {
            Storage.getInstance().getProdotti().clear();
        } else {
            // Per txt o database, elimina i record creati durante il test
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
        // Prepara un bean per il nuovo prodotto
        ProdottoBean bean = preparaProdottoBean("Pasta", "PRIMI", new BigDecimal("12"), true);

        // Aggiunge il prodotto tramite il controller
        controller.aggiungiProdotto(bean);

        // Recupera il prodotto tramite nome usando il metodo del controller
        ProdottoBean result = controller.getProdottoByNome("Pasta");
        assertNotNull(result, "Il prodotto non deve essere nullo.");
        createdProdottoIds.add(result.getId());

        // Verifica che i dati siano stati salvati correttamente
        assertTrue(result.getId() > 0);
        assertEquals("Pasta", result.getNome());
        // Confronta i prezzi usando compareTo per evitare differenze di scale (es. 12 vs. 12.00)
        assertEquals(0, result.getPrezzo().compareTo(new BigDecimal("12")));
        assertEquals("PRIMI", result.getCategoria());
        assertTrue(result.getDisponibile());
    }

    @Test
    void testModificaProdotto() {
        // Crea e salva un prodotto iniziale
        Prodotto prodotto = new Prodotto(0, "Fiorentina", new BigDecimal("75.00"), Categoria.SECONDI, true);
        prodottoDao.store(prodotto);
        int prodId = prodotto.getId();
        createdProdottoIds.add(prodId);

        // Prepara il bean con i dettagli aggiornati
        ProdottoBean bean = preparaProdottoBean(prodId, "Bistecca alla Fiorentina", "SECONDI", new BigDecimal("95.00"), false);
        controller.modificaProdotto(bean);

        // Recupera il prodotto aggiornato tramite il DAO
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
        // Aggiunge un prodotto di esempio
        Prodotto prodotto = new Prodotto(0, "Brodo di Carne", new BigDecimal("18.30"), Categoria.PRIMI, true);
        prodottoDao.store(prodotto);
        int prodId = prodotto.getId();
        createdProdottoIds.add(prodId);

        // Elimina il prodotto tramite il controller
        controller.eliminaProdotto(prodId);

        // Verifica che il prodotto non esista più
        assertFalse(prodottoDao.exists(prodId));
        createdProdottoIds.remove(Integer.valueOf(prodId));
    }

    @Test
    void testGetProdotti() {
        // Aggiunge diversi prodotti al DAO
        Prodotto p1 = new Prodotto(0, "Pizza Margherita", new BigDecimal("5.50"), Categoria.PIZZE, true);
        Prodotto p2 = new Prodotto(0, "Tiramisu", new BigDecimal("8.50"), Categoria.DOLCI, false);
        Prodotto p3 = new Prodotto(0, "Insalata Mista", new BigDecimal("6.00"), Categoria.ANTIPASTI, true);

        prodottoDao.store(p1);
        prodottoDao.store(p2);
        prodottoDao.store(p3);
        createdProdottoIds.add(p1.getId());
        createdProdottoIds.add(p2.getId());
        createdProdottoIds.add(p3.getId());

        // Recupera i prodotti disponibili tramite il controller
        List<ProdottoBean> prodottiDisponibili = controller.getProdotti();
        // Filtra solo i prodotti che sono stati creati durante il test
        List<ProdottoBean> createdProducts = prodottiDisponibili.stream()
                .filter(bean -> createdProdottoIds.contains(bean.getId()))
                .toList();
        // Dovrebbero essere presenti solo i prodotti creati con disponibile == true (p1 e p3)
        assertEquals(2, createdProducts.size(), "I prodotti creati e disponibili devono essere 2.");
        for (ProdottoBean bean : createdProducts) {
            assertTrue(bean.getDisponibile());
        }
    }

    @Test
    void testGetProdottoByNome() {
        // Aggiunge un prodotto specifico
        Prodotto prodotto = new Prodotto(0, "Gattò", new BigDecimal("15.00"), Categoria.SECONDI, true);
        prodottoDao.store(prodotto);
        int prodId = prodotto.getId();
        createdProdottoIds.add(prodId);

        // Recupera il prodotto tramite nome
        ProdottoBean bean = controller.getProdottoByNome("Gattò");
        assertNotNull(bean);
        assertEquals("Gattò", bean.getNome());
        assertEquals(0, bean.getPrezzo().compareTo(new BigDecimal("15.00")));
        assertEquals("SECONDI", bean.getCategoria());
        assertTrue(bean.getDisponibile());
    }

    // Test per la gestione degli errori

    @Test
    void testAggiungiProdottoConNomeVuoto() {
        ProdottoBean bean = preparaProdottoBean("   ", "PRIMI", new BigDecimal("12"), true);
        Exception exception = assertThrows(ProdottoException.class, () -> controller.aggiungiProdotto(bean));
        assertEquals("Il nome del prodotto non può essere vuoto.", exception.getMessage());
    }

    @Test
    void testAggiungiProdottoConCategoriaNonValida() {
        ProdottoBean bean = preparaProdottoBean("Salmone", " ", new BigDecimal("12"), true);
        Exception exception = assertThrows(ProdottoException.class, () -> controller.aggiungiProdotto(bean));
        assertEquals("Seleziona una categoria valida.", exception.getMessage());
    }

    @Test
    void testAggiungiProdottoConPrezzoNonValido() {
        ProdottoBean bean = preparaProdottoBean("Pizza Capricciosa", "PIZZE", new BigDecimal("-5"), true);
        Exception exception = assertThrows(ProdottoException.class, () -> controller.aggiungiProdotto(bean));
        assertEquals("Inserisci un valore numerico valido per il prezzo maggiore di zero.", exception.getMessage());
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

    // Metodi utilitari per preparare i bean

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