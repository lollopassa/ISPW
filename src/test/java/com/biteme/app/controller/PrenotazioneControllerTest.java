package com.biteme.app.controller;

import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.model.Prenotazione;
import com.biteme.app.persistence.inmemory.InMemoryPrenotazioneDao;
import com.biteme.app.persistence.inmemory.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PrenotazioneControllerTest {

    private PrenotazioneController controller;
    private InMemoryPrenotazioneDao inMemoryDao;

    @BeforeEach
    public void setUp() throws Exception {
        // Pulisce lo storage condiviso per garantire test indipendenti
        Storage.getInstance().getPrenotazioni().clear();

        // Istanzia il controller
        controller = new PrenotazioneController();

        // Crea una nuova istanza di InMemoryPrenotazioneDao
        inMemoryDao = new InMemoryPrenotazioneDao();

        // Usa reflection per iniettare l'istanza in-memory al posto di quella ottenuta da Configuration
        Field daoField = PrenotazioneController.class.getDeclaredField("prenotazioneDao");
        daoField.setAccessible(true);
        daoField.set(controller, inMemoryDao);
    }

    @Test
    public void testCreaPrenotazione() {
        // Prepara il bean di input usando LocalDate e LocalTime
        PrenotazioneBean bean = new PrenotazioneBean();
        // Per l'inserimento di una nuova prenotazione l'ID può essere 0 (o lasciato non valorizzato)
        bean.setId(0);
        bean.setNomeCliente("Mario Rossi");
        bean.setData(LocalDate.of(2025, 3, 15));
        bean.setOrario(LocalTime.of(20, 0)); // Imposta l'orario come LocalTime
        bean.setNote("Prenotazione per anniversario");
        bean.setTelefono("1234567890");
        bean.setCoperti(4);

        // Esegui il metodo da testare
        controller.creaPrenotazione(bean);

        // Recupera le prenotazioni dallo storage
        List<Prenotazione> prenotazioni = Storage.getInstance().getPrenotazioni();
        assertEquals(1, prenotazioni.size());

        Prenotazione p = prenotazioni.get(0);
        // L'ID dovrebbe essere stato assegnato dall'InMemoryPrenotazioneDao (maggiore di 0)
        assertTrue(p.getId() > 0);
        assertEquals("Mario Rossi", p.getNomeCliente());
        assertEquals(LocalDate.of(2025, 3, 15), p.getData());
        assertEquals(LocalTime.of(20, 0), p.getOrario());
        assertEquals("Prenotazione per anniversario", p.getNote());
        assertEquals("1234567890", p.getTelefono());
        assertEquals(4, p.getCoperti());
    }

    @Test
    public void testGetPrenotazioniByData() {
        // Inserisci una prenotazione direttamente tramite il DAO in-memory
        Prenotazione p = new Prenotazione(0, "Luigi Bianchi", LocalTime.of(19, 0),
                LocalDate.of(2025, 4, 10),
                "Cena romantica", "0987654321", 2);
        inMemoryDao.store(p);

        // Esegui il metodo da testare
        List<PrenotazioneBean> beans = controller.getPrenotazioniByData(LocalDate.of(2025, 4, 10));
        assertNotNull(beans);
        assertEquals(1, beans.size());

        PrenotazioneBean bean = beans.get(0);
        assertEquals("Luigi Bianchi", bean.getNomeCliente());
        assertEquals(LocalDate.of(2025, 4, 10), bean.getData());
        assertEquals(LocalTime.of(19, 0), bean.getOrario());
        assertEquals("Cena romantica", bean.getNote());
        assertEquals("0987654321", bean.getTelefono());
        assertEquals(2, bean.getCoperti());
    }

    @Test
    public void testModificaPrenotazione() {
        // Inserisci una prenotazione iniziale
        Prenotazione p = new Prenotazione(0, "Anna Verdi", LocalTime.of(21, 0),
                LocalDate.of(2025, 5, 20),
                "Prenotazione iniziale", "1122334455", 3);
        inMemoryDao.store(p);
        int storedId = p.getId();

        // Prepara il bean per la modifica (con lo stesso ID)
        PrenotazioneBean bean = new PrenotazioneBean();
        bean.setId(storedId);
        bean.setNomeCliente("Anna Verdi Modificata");
        bean.setData(LocalDate.of(2025, 5, 21)); // modifica la data
        bean.setOrario(LocalTime.of(22, 0)); // modifica l'orario
        bean.setNote("Prenotazione modificata");
        bean.setTelefono("1122334455");
        bean.setCoperti(4);

        // Esegui il metodo da testare
        controller.modificaPrenotazione(bean);

        // Verifica che la prenotazione in storage sia stata aggiornata
        Optional<Prenotazione> opt = inMemoryDao.load(storedId);
        assertTrue(opt.isPresent());
        Prenotazione updated = opt.get();
        assertEquals("Anna Verdi Modificata", updated.getNomeCliente());
        assertEquals(LocalDate.of(2025, 5, 21), updated.getData());
        assertEquals(LocalTime.of(22, 0), updated.getOrario());
        assertEquals("Prenotazione modificata", updated.getNote());
        assertEquals("1122334455", updated.getTelefono());
        assertEquals(4, updated.getCoperti());
    }

    @Test
    public void testEliminaPrenotazioneEsistente() {
        // Inserisci una prenotazione
        Prenotazione p = new Prenotazione(0, "Mario Rossi", LocalTime.of(20, 0),
                LocalDate.of(2025, 6, 10),
                "Da eliminare", "1234567890", 4);
        inMemoryDao.store(p);
        int storedId = p.getId();

        // Esegui il metodo di eliminazione tramite il controller
        controller.eliminaPrenotazione(storedId);

        // Verifica che la prenotazione sia stata rimossa
        assertFalse(inMemoryDao.exists(storedId));
    }

    @Test
    public void testEliminaPrenotazioneNonEsistente() {
        int nonExistingId = 999;
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            controller.eliminaPrenotazione(nonExistingId);
        });
        assertTrue(ex.getMessage().contains("non esiste"));
    }
}
