package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.model.Ordine;
import com.biteme.app.model.Prodotto;
import com.biteme.app.model.Categoria;
import com.biteme.app.persistence.inmemory.InMemoryOrdineDao;
import com.biteme.app.persistence.inmemory.InMemoryProdottoDao;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

//@author Lorenzo Passacantilli

class OrdineControllerTest {

    // Inizializza JavaFX (necessario per usare VBox, HBox, Label, ecc.)
    static {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX è già stato inizializzato
        }
    }

    private OrdineController controller;
    private InMemoryOrdineDao inMemoryOrdineDao;
    private InMemoryProdottoDao inMemoryProdottoDao;

    @BeforeEach
    void setUp() throws Exception {
        // Inizializza il controller
        controller = new OrdineController();

        // Crea le implementazioni in-memory per i DAO
        inMemoryOrdineDao = new InMemoryOrdineDao();
        inMemoryProdottoDao = new InMemoryProdottoDao();

        // Usa reflection per iniettare i DAO in-memory nella classe OrdineController
        Field ordineDaoField = OrdineController.class.getDeclaredField("ordineDao");
        ordineDaoField.setAccessible(true);
        ordineDaoField.set(controller, inMemoryOrdineDao);

        Field prodottoDaoField = OrdineController.class.getDeclaredField("prodottoDao");
        prodottoDaoField.setAccessible(true);
        prodottoDaoField.set(controller, inMemoryProdottoDao);
    }

    @AfterEach
    void tearDown() {
        // Pulizia dello storage se necessario
    }

    @Test
    void testSalvaOrdine() {
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setProdotti(List.of("Pizza Margherita", "Coca Cola"));
        ordineBean.setQuantita(List.of(2, 1));
        int id = 1;

        controller.salvaOrdine(ordineBean, id);

        Ordine ordine = inMemoryOrdineDao.getById(id);
        assertNotNull(ordine);
        assertEquals(id, ordine.getId());
        assertEquals(List.of("Pizza Margherita", "Coca Cola"), ordine.getProdotti());
        assertEquals(List.of(2, 1), ordine.getQuantita());
    }

    @Test
    void testGetOrdini() {
        Ordine ordine = new Ordine(
                1,
                List.of("Pizza Margherita"),
                List.of(2)
        );
        inMemoryOrdineDao.store(ordine);

        OrdineBean bean = controller.getOrdineById(1);
        assertNotNull(bean);
        assertEquals(1, bean.getId());
        assertEquals("Pizza Margherita", bean.getProdotti().get(0));
        assertEquals("2", String.valueOf(bean.getQuantita().get(0)));
    }

    @Test
    void testGetOrdineByIdNonEsistente() {
        // Modifica: ora ci aspettiamo un IllegalArgumentException con il messaggio corretto
        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.getOrdineById(999));
        assertEquals("Ordine con ID 999 non trovato", ex.getMessage());
    }

    @Test
    void testRecuperaQuantitaDalRiepilogo() {
        VBox riepilogo = new VBox();
        HBox hbox1 = new HBox();
        Label label1 = new Label("Pizza Margherita x 3");
        hbox1.getChildren().add(label1);
        riepilogo.getChildren().add(hbox1);
        HBox hbox2 = new HBox();
        Label label2 = new Label("Coca Cola x 2");
        hbox2.getChildren().add(label2);
        riepilogo.getChildren().add(hbox2);
        controller.setRiepilogoContenuto(riepilogo);

        int quantitaPizza = controller.recuperaQuantitaDalRiepilogo("Pizza Margherita");
        int quantitaCoca = controller.recuperaQuantitaDalRiepilogo("Coca Cola");
        int quantitaNonEsistente = controller.recuperaQuantitaDalRiepilogo("Pasta");

        assertEquals(3, quantitaPizza);
        assertEquals(2, quantitaCoca);
        assertEquals(0, quantitaNonEsistente);
    }

    @Test
    void testSalvaOrdineEStato() {
        VBox riepilogo = new VBox();
        HBox hbox = new HBox();
        Label label = new Label("Pizza Margherita x 3");
        hbox.getChildren().add(label);
        riepilogo.getChildren().add(hbox);
        controller.setRiepilogoContenuto(riepilogo);

        int ordineId = 1;
        controller.salvaOrdineEStato(ordineId, "IN_CORSO");

        Ordine ordine = inMemoryOrdineDao.getById(ordineId);
        assertNotNull(ordine);
        List<String> expectedProdotti = List.of("Pizza Margherita");
        List<Integer> expectedQuantita = List.of(3);
        assertEquals(expectedProdotti, ordine.getProdotti());
        assertEquals(expectedQuantita, ordine.getQuantita());
    }

    @Test
    void testSalvaOrdineEStatoInvalidState() {
        VBox riepilogo = new VBox();
        HBox hbox = new HBox();
        Label label = new Label("Pizza Margherita x 3");
        hbox.getChildren().add(label);
        riepilogo.getChildren().add(hbox);
        controller.setRiepilogoContenuto(riepilogo);

        // Ora ci aspettiamo un OrdineException, dato che il metodo incapsula l'IllegalArgumentException
        Exception ex = assertThrows(OrdineException.class,
                () -> controller.salvaOrdineEStato(1, "INVALID_STATE"));
        assertTrue(ex.getMessage().contains("Stato ordine non valido"));
    }

    @Test
    void testGetProdottiByCategoria(){
        // Creiamo due prodotti con categorie differenti
        Prodotto prod1 = new Prodotto(1, "Pizza Margherita", new BigDecimal("8.50"), Categoria.PIZZE, true);
        Prodotto prod2 = new Prodotto(2, "Pasta al Pomodoro", new BigDecimal("7.00"), Categoria.PRIMI, true);

        inMemoryProdottoDao.store(prod1);
        inMemoryProdottoDao.store(prod2);

        // Verifichiamo che il metodo ritorni solo il prodotto della categoria "PIZZE"
        List<ProdottoBean> prodottiPizza = controller.getProdottiByCategoria("PIZZE");
        assertEquals(1, prodottiPizza.size());
        ProdottoBean bean1 = prodottiPizza.get(0);
        assertEquals(1, bean1.getId());
        assertEquals("Pizza Margherita", bean1.getNome());
        assertEquals("8.50", bean1.getPrezzo().toString());
        assertEquals("PIZZE", bean1.getCategoria());
        assertTrue(bean1.getDisponibile());
    }

    @Test
    void testGetTuttiProdotti() {
        Prodotto prod1 = new Prodotto(1, "Pizza Margherita", new BigDecimal("8.50"), Categoria.PIZZE, true);
        Prodotto prod2 = new Prodotto(2, "Pasta al Pomodoro", new BigDecimal("7.00"), Categoria.PRIMI, true);

        inMemoryProdottoDao.store(prod1);
        inMemoryProdottoDao.store(prod2);

        List<ProdottoBean> allProdotti = controller.getTuttiProdotti();
        assertEquals(2, allProdotti.size());
    }
}
