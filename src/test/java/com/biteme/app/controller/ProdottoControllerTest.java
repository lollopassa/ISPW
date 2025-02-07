package com.biteme.app.controller;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.exception.ProdottoException;
import com.biteme.app.model.Prodotto;
import com.biteme.app.model.Categoria;
import com.biteme.app.persistence.inmemory.InMemoryProdottoDao;
import com.biteme.app.persistence.inmemory.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

//@author Kevin Hoxha

class ProdottoControllerTest {

    private ProdottoController controller;
    private InMemoryProdottoDao inMemoryDao;

    @BeforeEach
    void setUp() throws Exception {
        // Pulisce lo storage condiviso dei prodotti per garantire test indipendenti
        Storage.getInstance().getProdotti().clear();

        // Istanzia il controller
        controller = new ProdottoController();

        // Crea una nuova istanza in‑memory del DAO per i prodotti
        inMemoryDao = new InMemoryProdottoDao();

        // Usa reflection per iniettare l'istanza in‑memory al posto di quella ottenuta tramite Configuration
        Field daoField = ProdottoController.class.getDeclaredField("prodottoDao");
        daoField.setAccessible(true);
        daoField.set(controller, inMemoryDao);
    }

    // Test di successo

    @Test
    void testAggiungiProdotto() {
        // Prepara il bean del prodotto
        ProdottoBean bean = new ProdottoBean();
        bean.setId(0); // Nuovo prodotto, id non ancora assegnato
        bean.setNome("Pasta");
        bean.setPrezzo(new BigDecimal("12"));
        bean.setCategoria("PRIMI");
        bean.setDisponibile(true);

        // Esegue il metodo da testare
        controller.aggiungiProdotto(bean);

        // Recupera i prodotti dallo storage
        List<Prodotto> prodotti = Storage.getInstance().getProdotti();
        assertEquals(1, prodotti.size());

        Prodotto p = prodotti.get(0);
        // L'id dovrebbe essere stato assegnato (maggiore di 0)
        assertTrue(p.getId() > 0);
        assertEquals("Pasta", p.getNome());
        assertEquals(new BigDecimal("12"), p.getPrezzo());
        // Il controller converte la stringa in enum, per cui il confronto avviene con l'enum
        assertEquals(Categoria.PRIMI, p.getCategoria());
        assertTrue(p.isDisponibile());
    }

    @Test
    void testGetProdottoByNome() {
        // Inserisce un prodotto direttamente tramite il DAO in‑memory
        Prodotto p = new Prodotto(0, "Pasta", new BigDecimal("12.50"), Categoria.PRIMI, true);
        inMemoryDao.store(p);

        // Esegue il metodo da testare
        ProdottoBean bean = controller.getProdottoByNome("Pasta");
        assertNotNull(bean);
        assertEquals("Pasta", bean.getNome());
        assertEquals(new BigDecimal("12.50"), bean.getPrezzo());
        assertEquals("PRIMI", bean.getCategoria());
        assertTrue(bean.getDisponibile());
    }

    @Test
    void testGetProdotti() {
        // Inserisce alcuni prodotti: alcuni disponibili, altri no
        Prodotto p1 = new Prodotto(0, "Margherita", new BigDecimal("5.50"), Categoria.PIZZE, true);
        Prodotto p2 = new Prodotto(0, "Tiramisu", new BigDecimal("8"), Categoria.DOLCI, false);
        Prodotto p3 = new Prodotto(0, "Fritto Misto", new BigDecimal("18.30"), Categoria.ANTIPASTI, true);

        inMemoryDao.store(p1);
        inMemoryDao.store(p2);
        inMemoryDao.store(p3);

        // Il metodo getProdotti() restituisce solo i prodotti disponibili
        List<ProdottoBean> prodottiBean = controller.getProdotti();
        assertEquals(2, prodottiBean.size());
        for (ProdottoBean bean : prodottiBean) {
            assertTrue(bean.getDisponibile());
        }
    }

    @Test
    void testModificaProdotto() {
        // Inserisce un prodotto iniziale
        Prodotto p = new Prodotto(0, "Fiorentina", new BigDecimal("75.00"), Categoria.SECONDI, true);
        inMemoryDao.store(p);
        int storedId = p.getId();

        // Prepara il bean per la modifica
        ProdottoBean bean = new ProdottoBean();
        bean.setId(storedId);
        bean.setNome("Bistecca alla Fiorentina");
        bean.setPrezzo(new BigDecimal("95.00"));
        bean.setCategoria("SECONDI");
        bean.setDisponibile(false);

        // Esegue la modifica
        controller.modificaProdotto(bean);

        // Verifica che il prodotto sia stato aggiornato
        Optional<Prodotto> opt = inMemoryDao.load(storedId);
        assertTrue(opt.isPresent());
        Prodotto updated = opt.get();
        assertEquals("Bistecca alla Fiorentina", updated.getNome());
        assertEquals(new BigDecimal("95.00"), updated.getPrezzo());
        assertEquals(Categoria.SECONDI, updated.getCategoria());
        assertFalse(updated.isDisponibile());
    }

    @Test
    void testEliminaProdotto() {
        Prodotto p = new Prodotto(0, "Brodo Vegetale", new BigDecimal("18.99"), Categoria.PRIMI, true);
        inMemoryDao.store(p);
        int storedId = p.getId();
        controller.eliminaProdotto(storedId);
        assertFalse(inMemoryDao.exists(storedId));
    }

    @Test
    void testAggiungiProdottoConNomeVuoto() {
        Exception ex = assertThrows(ProdottoException.class, this::aggiungiProdottoConNomeVuoto);
        assertTrue(ex.getMessage().contains("Il nome del prodotto non può essere vuoto."));
    }

    void aggiungiProdottoConNomeVuoto() {
        ProdottoBean bean = new ProdottoBean();
        bean.setId(0);
        bean.setNome("   "); // Nome vuoto
        bean.setPrezzo(new BigDecimal("10"));
        bean.setCategoria("PIZZE");
        bean.setDisponibile(true);
        controller.aggiungiProdotto(bean);
    }

    @Test
    void testAggiungiProdottoConCategoriaVuota() {
        Exception ex = assertThrows(ProdottoException.class, this::aggiungiProdottoConCategoriaVuota);
        assertTrue(ex.getMessage().contains("Seleziona una categoria valida."));
    }

    void aggiungiProdottoConCategoriaVuota() {
        ProdottoBean bean = new ProdottoBean();
        bean.setId(0);
        bean.setNome("Margherita");
        bean.setPrezzo(new BigDecimal("8.5"));
        bean.setCategoria("   "); // Categoria vuota
        bean.setDisponibile(true);
        controller.aggiungiProdotto(bean);
    }

    @Test
    void testAggiungiProdottoConPrezzoNonValido() {
        Exception ex = assertThrows(ProdottoException.class, this::aggiungiProdottoConPrezzoNonValido);
        assertTrue(ex.getMessage().contains("Inserisci un valore numerico valido per il prezzo maggiore di zero."));
    }

    void aggiungiProdottoConPrezzoNonValido() {
        ProdottoBean bean = new ProdottoBean();
        bean.setId(0);
        bean.setNome("Margherita");
        bean.setPrezzo(new BigDecimal("-5")); // Prezzo negativo
        bean.setCategoria("PIZZE");
        bean.setDisponibile(true);
        controller.aggiungiProdotto(bean);
    }

    @Test
    void testGetProdottoByNomeNonTrovato() {
        Exception ex = assertThrows(ProdottoException.class, this::getProdottoByNomeNonTrovato);
        assertTrue(ex.getMessage().contains("Prodotto con nome"));
    }

    void getProdottoByNomeNonTrovato() {
        controller.getProdottoByNome("NonEsistente");
    }

    @Test
    void testModificaProdottoConIdNonValido() {
        Exception ex = assertThrows(ProdottoException.class, this::modificaProdottoConIdNonValido);
        assertTrue(ex.getMessage().contains("L'ID del prodotto non è valido."));
    }

    void modificaProdottoConIdNonValido() {
        ProdottoBean bean = new ProdottoBean();
        bean.setId(0); // ID non valido per modifica
        bean.setNome("Nuovo Nome");
        bean.setPrezzo(new BigDecimal("10"));
        bean.setCategoria("PIZZE");
        bean.setDisponibile(true);
        controller.modificaProdotto(bean);
    }

    @Test
    void testEliminaProdottoConIdNonValido() {
        Exception ex = assertThrows(ProdottoException.class, this::eliminaProdottoConIdNonValido);
        assertTrue(ex.getMessage().contains("L'ID del prodotto da eliminare non è valido."));
    }

    void eliminaProdottoConIdNonValido() {
        controller.eliminaProdotto(null);
    }
}