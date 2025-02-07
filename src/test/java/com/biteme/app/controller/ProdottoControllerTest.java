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

//@Author Kevin Hoxha

class ProdottoControllerTest {

    private ProdottoController controller;
    private InMemoryProdottoDao inMemoryDao;

    @BeforeEach
    void setUp() throws Exception {
        // Pulisce lo storage condiviso dei prodotti
        Storage.getInstance().getProdotti().clear();

        // Crea un'istanza del controller e imposta il DAO in-memory
        controller = new ProdottoController();
        inMemoryDao = new InMemoryProdottoDao();

        // Usa reflection per iniettare l'istanza in-memory nel controller
        Field daoField = ProdottoController.class.getDeclaredField("prodottoDao");
        daoField.setAccessible(true);
        daoField.set(controller, inMemoryDao);
    }

    // Test di aggiunta con successo
    @Test
    void testAggiungiProdotto() {
        ProdottoBean bean = preparaProdottoBean("Pasta", "PRIMI", new BigDecimal("12"), true);

        controller.aggiungiProdotto(bean);

        // Verifica che il prodotto sia stato aggiunto correttamente
        List<Prodotto> prodotti = Storage.getInstance().getProdotti();
        assertEquals(1, prodotti.size());
        Prodotto prodotto = prodotti.get(0);

        // Assicurati che i valori siano corretti
        assertTrue(prodotto.getId() > 0);
        assertEquals("Pasta", prodotto.getNome());
        assertEquals(new BigDecimal("12"), prodotto.getPrezzo());
        assertEquals(Categoria.PRIMI, prodotto.getCategoria());
        assertTrue(prodotto.isDisponibile());
    }

    @Test
    void testModificaProdotto() {
        // Carica un prodotto iniziale
        Prodotto prodotto = new Prodotto(0, "Fiorentina", new BigDecimal("75.00"), Categoria.SECONDI, true);
        inMemoryDao.store(prodotto);

        // Prepara il bean con i dettagli aggiornati
        ProdottoBean bean = preparaProdottoBean(prodotto.getId(), "Bistecca alla Fiorentina", "SECONDI", new BigDecimal("95.00"), false);

        controller.modificaProdotto(bean);

        // Assicurati che il prodotto sia stato aggiornato
        Optional<Prodotto> updatedProdotto = inMemoryDao.load(prodotto.getId());
        assertTrue(updatedProdotto.isPresent());

        Prodotto p = updatedProdotto.get();
        assertEquals("Bistecca alla Fiorentina", p.getNome());
        assertEquals(new BigDecimal("95.00"), p.getPrezzo());
        assertEquals(Categoria.SECONDI, p.getCategoria());
        assertFalse(p.isDisponibile());
    }

    @Test
    void testEliminaProdotto() {
        // Aggiunge un prodotto di esempio
        Prodotto prodotto = new Prodotto(0, "Brodo di Carne", new BigDecimal("18.30"), Categoria.PRIMI, true);
        inMemoryDao.store(prodotto);

        // Recupera l'ID assegnato al prodotto
        int prodottoId = prodotto.getId();

        // Esegue l'eliminazione
        controller.eliminaProdotto(prodottoId);

        // Assicurati che il prodotto sia stato rimosso dallo storage
        assertFalse(inMemoryDao.exists(prodottoId));
    }

    @Test
    void testGetProdotti() {
        // Aggiunge diversi prodotti al DAO
        Prodotto p1 = new Prodotto(0, "Pizza Margherita", new BigDecimal("5.50"), Categoria.PIZZE, true);
        Prodotto p2 = new Prodotto(0, "Tiramisu", new BigDecimal("8.50"), Categoria.DOLCI, false);
        Prodotto p3 = new Prodotto(0, "Insalata Mista", new BigDecimal("6.00"), Categoria.ANTIPASTI, true);

        inMemoryDao.store(p1);
        inMemoryDao.store(p2);
        inMemoryDao.store(p3);

        // Recupera i prodotti disponibili
        List<ProdottoBean> prodottiDisponibili = controller.getProdotti();
        assertEquals(2, prodottiDisponibili.size()); // Devono essere solo quelli disponibili

        // Assicura che ogni prodotto recuperato sia disponibile
        for (ProdottoBean bean : prodottiDisponibili) {
            assertTrue(bean.getDisponibile());
        }
    }

    @Test
    void testGetProdottoByNome() {
        // Aggiunge un prodotto specifico
        Prodotto prodotto = new Prodotto(0, "Gattò", new BigDecimal("15.00"), Categoria.SECONDI, true);
        inMemoryDao.store(prodotto);

        // Recupera il prodotto tramite nome
        ProdottoBean bean = controller.getProdottoByNome("Gattò");

        // Assicurati che i dettagli corrispondano
        assertNotNull(bean);
        assertEquals("Gattò", bean.getNome());
        assertEquals(new BigDecimal("15.00"), bean.getPrezzo());
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

    // Metodo Utilitario
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