package com.biteme.app.controller;

import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.exception.ValidationException;
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

//@author Kevin Hoxha

class PrenotazioneControllerTest {

    private PrenotazioneController controller;
    private InMemoryPrenotazioneDao inMemoryDao;

    @BeforeEach
    void setUp() throws Exception {
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
    void testCreaPrenotazione() {
        controller.creaPrenotazione(
                "Mario Rossi",
                "20:00",
                LocalDate.of(2025, 3, 15),
                "1234567890",
                "Prenotazione per anniversario",
                "4"
        );

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

    // ===============================
    // Test per i casi d'errore (runtime exception)
    // Ogni assertThrows invoca un singolo metodo helper
    // ===============================

    @Test
    void testCreaPrenotazioneConNomeVuoto() {
        Exception ex = assertThrows(ValidationException.class, this::creaPrenotazioneConNomeVuoto);
        assertTrue(ex.getMessage().contains("Il nome del cliente non puÃ² essere vuoto."));
    }

    void creaPrenotazioneConNomeVuoto() {
        controller.creaPrenotazione(
                "   ", // Nome vuoto
                "20:00",
                LocalDate.of(2025, 3, 15),
                "1234567890",
                "Test",
                "4"
        );
    }

    @Test
    void testCreaPrenotazioneConDataNulla() {
        Exception ex = assertThrows(ValidationException.class, this::creaPrenotazioneConDataNulla);
        assertTrue(ex.getMessage().contains("Seleziona una data valida."));
    }

    void creaPrenotazioneConDataNulla() {
        controller.creaPrenotazione(
                "Mario Rossi",
                "20:00",
                null, // Data nulla
                "1234567890",
                "Test",
                "4"
        );
    }

    @Test
    void testCreaPrenotazioneConOrarioVuoto() {
        Exception ex = assertThrows(ValidationException.class, this::creaPrenotazioneConOrarioVuoto);
        assertTrue(ex.getMessage().contains("Inserisci un orario valido."));
    }

    void creaPrenotazioneConOrarioVuoto() {
        controller.creaPrenotazione(
                "Mario Rossi",
                "   ", // Orario vuoto
                LocalDate.of(2025, 3, 15),
                "1234567890",
                "Test",
                "4"
        );
    }

    @Test
    void testCreaPrenotazioneConTelefonoNonValido() {
        Exception ex = assertThrows(ValidationException.class, this::creaPrenotazioneConTelefonoNonValido);
        assertTrue(ex.getMessage().contains("Il telefono deve contenere solo numeri."));
    }

    void creaPrenotazioneConTelefonoNonValido() {
        controller.creaPrenotazione(
                "Mario Rossi",
                "20:00",
                LocalDate.of(2025, 3, 15),
                "abc123", // Telefono non valido
                "Test",
                "4"
        );
    }

    @Test
    void testCreaPrenotazioneConCopertiNegativi() {
        Exception ex = assertThrows(ValidationException.class, this::creaPrenotazioneConCopertiNegativi);
        assertTrue(ex.getMessage().contains("I coperti devono essere maggiori di 0."));
    }

    void creaPrenotazioneConCopertiNegativi() {
        controller.creaPrenotazione(
                "Mario Rossi",
                "20:00",
                LocalDate.of(2025, 3, 15),
                "1234567890",
                "Test",
                "-1" // Coperti negativi
        );
    }

    @Test
    void testEliminaPrenotazioneNonEsistente() {
        Exception ex = assertThrows(IllegalArgumentException.class, this::eliminaPrenotazioneNonEsistente);
        assertTrue(ex.getMessage().contains("non esiste"));
    }

    void eliminaPrenotazioneNonEsistente() {
        controller.eliminaPrenotazione(999);
    }

    // ===============================
    // Test per i metodi non eccezionali
    // ===============================

    @Test
    void testGetPrenotazioniByData() {
        // Inserisce una prenotazione direttamente tramite il DAO in-memory
        Prenotazione p = new Prenotazione(
                0,
                "Luigi Bianchi",
                LocalTime.of(19, 0),
                LocalDate.of(2025, 4, 10),
                "Cena romantica",
                "0987654321",
                2
        );
        inMemoryDao.store(p);

        // Esegue il metodo da testare
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
    void testModificaPrenotazione() {
        // Inserisce una prenotazione iniziale
        Prenotazione p = new Prenotazione(
                0,
                "Anna Verdi",
                LocalTime.of(21, 0),
                LocalDate.of(2025, 5, 20),
                "Prenotazione iniziale",
                "1122334455",
                3
        );
        inMemoryDao.store(p);
        int storedId = p.getId();

        // Esegue la modifica con parametri validi
        PrenotazioneBean updatedBean = controller.modificaPrenotazione(
                storedId,
                "Anna Verdi Modificata",
                "22:00",
                LocalDate.of(2025, 5, 21),
                "1122334455",
                "Prenotazione modificata",
                "4"
        );

        assertNotNull(updatedBean);
        assertEquals(storedId, updatedBean.getId());
        assertEquals("Anna Verdi Modificata", updatedBean.getNomeCliente());
        assertEquals(LocalDate.of(2025, 5, 21), updatedBean.getData());
        assertEquals(LocalTime.of(22, 0), updatedBean.getOrario());
        assertEquals("Prenotazione modificata", updatedBean.getNote());
        assertEquals("1122334455", updatedBean.getTelefono());
        assertEquals(4, updatedBean.getCoperti());

        // Verifica che il DAO contenga l'aggiornamento
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
    void testEliminaPrenotazioneEsistente() {
        // Inserisce una prenotazione
        Prenotazione p = new Prenotazione(
                0,
                "Mario Rossi",
                LocalTime.of(20, 0),
                LocalDate.of(2025, 6, 10),
                "Da eliminare",
                "1234567890",
                4
        );
        inMemoryDao.store(p);
        int storedId = p.getId();

        // Esegue il metodo di eliminazione tramite il controller
        controller.eliminaPrenotazione(storedId);

        // Verifica che la prenotazione sia stata rimossa
        assertFalse(inMemoryDao.exists(storedId));
    }
}