package com.biteme.app.controller;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entities.Ordine;
import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.entities.Categoria;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.entities.TipoOrdinazione;
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
    private OrdinazioneDao ordinazioneDao;  // DAO per il record padre, necessario per la FK
    // Variabile in cui salvare l'id generato per l'ordinazione (record padre)
    private int parentId;

    @BeforeEach
    void setUp() throws Exception {
        controller = new OrdineController();
        // Ottieni i DAO generici dalla configurazione
        ordineDao = Configuration.getPersistenceProvider().getDaoFactory().getOrdineDao();
        prodottoDao = Configuration.getPersistenceProvider().getDaoFactory().getProdottoDao();
        ordinazioneDao = Configuration.getPersistenceProvider().getDaoFactory().getOrdinazioneDao();

        // Injection dei DAO nel controller tramite reflection
        injectDao("ordineDao", ordineDao);
        injectDao("prodottoDao", prodottoDao);

        // --- Preparazione del record padre per l'ordine ---
        // Creiamo SEMPRE una nuova ordinazione dummy
        Ordinazione dummy = new Ordinazione(
                0,                    // id = 0 per auto-increment
                "Alberto Verdi",      // nome fittizio
                "1",                  // numero clienti fittizio
                TipoOrdinazione.ASPORTO,   // valore arbitrario
                "None",
                StatoOrdinazione.NUOVO,
                "12:00"
        );
        // Salviamo la dummy nel database
        ordinazioneDao.store(dummy);

        // Recupera l'id della ordinazione appena creata (verrà assegnato automaticamente dal DB)
        parentId = dummy.getId();

        // --- Pulizia dei prodotti (per evitare duplicati) ---
        removeProdottoIfExists("Margherita");
        removeProdottoIfExists("Pasta al Pomodoro");
    }

    @AfterEach
    void tearDown(){
        // Se presente, rimuovo il record dell'ordine (figlio) dalla persistenza txt
        if (ordineDao.exists(parentId)) {
            ordineDao.delete(parentId);
        }

        // Rimuovo il record padre (ordinazione) creato nel setUp()
        if (ordinazioneDao.exists(parentId)) {
            ordinazioneDao.delete(parentId);
        }

        // Pulisco anche i prodotti eventualmente inseriti (per evitare duplicati nei test successivi)
        removeProdottoIfExists("Pizza Margherita");
        removeProdottoIfExists("Pasta al Pomodoro");
    }





    private void injectDao(String fieldName, Object dao) throws Exception {
        Field field = OrdineController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, dao);
    }


    private void removeProdottoIfExists(String nome) {
        List<Prodotto> prodotti = prodottoDao.getAll();
        prodotti.stream()
                .filter(p -> p.getNome().equals(nome))
                .forEach(p -> prodottoDao.delete(p.getId()));
    }



    @Test
    void testSalvaOrdine() {
        // Crea un OrdineBean con i dati da salvare
        OrdineBean ordineBean = new OrdineBean();
        ordineBean.setProdotti(List.of("Pizza Margherita", "Coca Cola"));
        ordineBean.setQuantita(List.of(2, 1));

        // Il secondo parametro utilizza l'id dell'ordinazione appena creato (parentId)
        controller.salvaOrdine(ordineBean, parentId);

        // In una relazione one-to-one, l'id dell'ordine deve corrispondere a quello dell'ordinazione (parent)
        Ordine ordine = ordineDao.getById(parentId);
        assertNotNull(ordine, "L'ordine non dovrebbe essere null");
        assertEquals(parentId, ordine.getId(), "L'ID dell'ordine deve essere uguale all'id del record padre");
        assertEquals(List.of("Pizza Margherita", "Coca Cola"), ordine.getProdotti(), "I prodotti non coincidono");
        assertEquals(List.of(2, 1), ordine.getQuantita(), "Le quantità non coincidono");
    }

    @Test
    void testGetOrdini() {
        // Crea un ordine utilizzando il parentId generato nel setUp(), che esiste nella tabella 'ordinazione'
        Ordine ordineDaSalvare = new Ordine(parentId, List.of("Pizza Margherita"), List.of(2));
        ordineDao.store(ordineDaSalvare);
        int storedId = ordineDaSalvare.getId(); // Questo sarà uguale a parentId

        // Recupera l'ordine tramite il controller
        OrdineBean bean = controller.getOrdineById(storedId);
        assertNotNull(bean, "L'ordine deve essere presente.");
        assertEquals(storedId, bean.getId());
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
        // Salva due prodotti: uno nella categoria PIZZE e uno in PRIMI.
        Prodotto prodotto1 = new Prodotto(0, "Pizza Margherita", new BigDecimal("8.50"), Categoria.PIZZE, true);
        Prodotto prodotto2 = new Prodotto(0, "Pasta al Pomodoro", new BigDecimal("7.00"), Categoria.PRIMI, true);
        prodottoDao.store(prodotto1);
        prodottoDao.store(prodotto2);

        // Recupera i prodotti tramite il controller filtrando per categoria "PIZZE"
        List<ProdottoBean> prodotti = controller.getProdottiByCategoria("PIZZE");
        // Filtra per ottenere solo il prodotto inserito (con nome "Pizza Margherita")
        List<ProdottoBean> filtered = prodotti.stream()
                .filter(p -> p.getNome().equals("Pizza Margherita"))
                .toList();
        assertEquals(1, filtered.size(), "Dovrebbe esserci un solo prodotto nella categoria PIZZE.");
        ProdottoBean bean = filtered.get(0);
        assertEquals("Pizza Margherita", bean.getNome());
        assertEquals("8.50", bean.getPrezzo().toString());
    }

    @Test
    void testGetTuttiProdotti() {
        // Salva due prodotti; eventuali record precedenti con lo stesso nome sono stati rimossi in setUp
        Prodotto prodotto1 = new Prodotto(0, "Pizza Margherita", new BigDecimal("8.50"), Categoria.PIZZE, true);
        Prodotto prodotto2 = new Prodotto(0, "Pasta al Pomodoro", new BigDecimal("7.00"), Categoria.PRIMI, true);
        prodottoDao.store(prodotto1);
        prodottoDao.store(prodotto2);

        List<ProdottoBean> prodotti = controller.getTuttiProdotti();
        // Filtra per includere solamente i prodotti inseriti (basandosi sul nome)
        List<ProdottoBean> filtered = prodotti.stream()
                .filter(p -> p.getNome().equals("Pizza Margherita") || p.getNome().equals("Pasta al Pomodoro"))
                .toList();
        assertEquals(2, filtered.size(), "Dovrebbero essere presenti 2 prodotti corrispondenti.");
    }
}
