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

import static org.junit.jupiter.api.Assertions.*;
//@author Kevin Hoxha
class PrenotazioneControllerTest {

    private PrenotazioneController controller;
    private InMemoryPrenotazioneDao inMemoryDao;

    @BeforeEach
    void setUp() throws Exception {
        // Pulisce lo storage condiviso per garantire test isolati
        Storage.getInstance().getPrenotazioni().clear();

        // Inizializza il controller
        controller = new PrenotazioneController();

        // Crea una nuova InMemoryPrenotazioneDao per mockare la persistenza
        inMemoryDao = new InMemoryPrenotazioneDao();

        // Usa reflection per iniettare il DAO in-memory nella classe PrenotazioneController
        Field daoField = PrenotazioneController.class.getDeclaredField("prenotazioneDao");
        daoField.setAccessible(true);
        daoField.set(controller, inMemoryDao);
    }

    @Test
    void testCreaPrenotazione() {
        controller.creaPrenotazione(
                "Mario Rossi",
                "20:00",
                LocalDate.of(2025, 4, 15),
                "1234567890",
                "Cena di compleanno",
                "3"
        );

        List<Prenotazione> prenotazioni = Storage.getInstance().getPrenotazioni();
        assertEquals(1, prenotazioni.size());

        Prenotazione p = prenotazioni.get(0);

        assertTrue(p.getId() > 0);
        assertEquals("Mario Rossi", p.getNomeCliente());
        assertEquals(LocalDate.of(2025, 4, 15), p.getData());
        assertEquals(LocalTime.of(20, 0), p.getOrario());
        assertEquals("Cena di compleanno", p.getNote());
        assertEquals("1234567890", p.getTelefono());
        assertEquals(3, p.getCoperti());
    }

    @Test
    void testModificaPrenotazione() {
        Prenotazione initPrenotazione = new Prenotazione(
                0,
                "Anna Verdi",
                LocalTime.of(19, 0),
                LocalDate.of(2025, 5, 20),
                "Cena di lavoro",
                "3345678912",
                4
        );
        inMemoryDao.store(initPrenotazione);
        int storedId = initPrenotazione.getId();

        PrenotazioneBean updatedBean = controller.modificaPrenotazione(
                storedId,
                "Anna Verdi Modificata",
                "20:30",
                LocalDate.of(2025, 5, 25),
                "3345678912",
                "Cena privata",
                "5"
        );

        assertNotNull(updatedBean);
        assertEquals(storedId, updatedBean.getId());
        assertEquals("Anna Verdi Modificata", updatedBean.getNomeCliente());
        assertEquals(LocalTime.of(20, 30), updatedBean.getOrario());
        assertEquals(LocalDate.of(2025, 5, 25), updatedBean.getData());
        assertEquals("Cena privata", updatedBean.getNote());
        assertEquals("3345678912", updatedBean.getTelefono());
        assertEquals(5, updatedBean.getCoperti());
    }

    @Test
    void testEliminaPrenotazione() {
        Prenotazione prenotazione = new Prenotazione(
                0,
                "Mario Rossi",
                LocalTime.of(20, 0),
                LocalDate.of(2025, 6, 15),
                "Cena con amici",
                "1234567890",
                4
        );
        inMemoryDao.store(prenotazione);
        int storedId = prenotazione.getId();

        controller.eliminaPrenotazione(storedId);

        assertFalse(inMemoryDao.exists(storedId));
    }

    @Test
    void testGetPrenotazioniByData() {
        Prenotazione p = new Prenotazione(
                0,
                "Giovanna Bianchi",
                LocalTime.of(19, 30),
                LocalDate.of(2025, 7, 20),
                "Festa privata",
                "3345678912",
                6
        );
        inMemoryDao.store(p);

        List<PrenotazioneBean> prenotazioni = controller.getPrenotazioniByData(LocalDate.of(2025, 7, 20));
        assertNotNull(prenotazioni);
        assertEquals(1, prenotazioni.size());

        PrenotazioneBean bean = prenotazioni.get(0);

        assertEquals("Giovanna Bianchi", bean.getNomeCliente());
        assertEquals(LocalDate.of(2025, 7, 20), bean.getData());
        assertEquals(LocalTime.of(19, 30), bean.getOrario());
        assertEquals("Festa privata", bean.getNote());
        assertEquals("3345678912", bean.getTelefono());
        assertEquals(6, bean.getCoperti());
    }

    @Test
    void testCreaPrenotazioneConNomeVuoto() {
        assertThrowsValidationException("Il nome del cliente non può essere vuoto.", "   ", "20:00", LocalDate.of(2025, 3, 15), "1234567890", "Test", "3");
    }

    @Test
    void testCreaPrenotazioneConOrarioNonValido() {
        assertThrowsValidationException("Formato orario non valido. Usa 'HH:mm'.", "Mario Rossi", "invalid", LocalDate.of(2025, 3, 15), "1234567890", "Test", "3");
    }

    @Test
    void testCreaPrenotazioneConDataNulla() {
        assertThrowsValidationException("Seleziona una data valida.", "Mario Rossi", "20:00", null, "1234567890", "Test", "3");
    }

    @Test
    void testCreaPrenotazioneConTelefonoNonValido() {
        assertThrowsValidationException("Il telefono deve contenere solo numeri.", "Mario Rossi", "20:00", LocalDate.of(2025, 3, 15), "invalid_123", "Test", "3");
    }

    @Test
    void testCreaPrenotazioneConCopertiNegativi() {
        assertThrowsValidationException("I coperti devono essere maggiori di 0.", "Mario Rossi", "20:00", LocalDate.of(2025, 3, 15), "1234567890", "Test", "-1");
    }

    @Test
    void testEliminaPrenotazioneNonEsistente() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.eliminaPrenotazione(999));
        assertEquals("La prenotazione con ID 999 non esiste.", ex.getMessage());
    }

    // Metodo helper per centralizzare la gestione delle eccezioni
    private void assertThrowsValidationException(String expectedMessage, String nome, String orario, LocalDate data, String telefono, String note, String coperti) {
        Exception ex = assertThrows(ValidationException.class, () -> {
            controller.creaPrenotazione(nome, orario, data, telefono, note, coperti);
        });
        assertEquals(expectedMessage, ex.getMessage());
    }
}