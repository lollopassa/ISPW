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

    // ===============================
    // Test di successo
    // ===============================

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

        // Recupera le prenotazioni per verificarne la persistenza
        List<Prenotazione> prenotazioni = Storage.getInstance().getPrenotazioni();
        assertEquals(1, prenotazioni.size());

        Prenotazione p = prenotazioni.get(0);

        // Verifica che i valori inseriti siano corretti
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
        // Inserisce una prenotazione iniziale
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

        // Modifica i dettagli della prenotazione
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
        // Inserisce una prenotazione
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

        // Elimina la prenotazione tramite il controller
        controller.eliminaPrenotazione(storedId);

        // Assicurati che sia stata effettivamente eliminata
        assertFalse(inMemoryDao.exists(storedId));
    }

    @Test
    void testGetPrenotazioniByData() {
        // Aggiunge una prenotazione direttamente nel DAO in-memory
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

        // Recupera le prenotazioni per una data specifica
        List<PrenotazioneBean> prenotazioni = controller.getPrenotazioniByData(LocalDate.of(2025, 7, 20));
        assertNotNull(prenotazioni);
        assertEquals(1, prenotazioni.size());

        PrenotazioneBean bean = prenotazioni.get(0);

        // Verifica i dettagli della prenotazione
        assertEquals("Giovanna Bianchi", bean.getNomeCliente());
        assertEquals(LocalDate.of(2025, 7, 20), bean.getData());
        assertEquals(LocalTime.of(19, 30), bean.getOrario());
        assertEquals("Festa privata", bean.getNote());
        assertEquals("3345678912", bean.getTelefono());
        assertEquals(6, bean.getCoperti());
    }

    // ===============================
    // Test per validazioni e errori
    // ===============================

    @Test
    void testCreaPrenotazioneConNomeVuoto() {
        Exception ex = assertThrows(ValidationException.class, () -> controller.creaPrenotazione(
                "   ",
                "20:00",
                LocalDate.of(2025, 3, 15),
                "1234567890",
                "Test",
                "3"
        ));
        assertEquals("Il nome del cliente non può essere vuoto.", ex.getMessage());
    }

    @Test
    void testCreaPrenotazioneConOrarioNonValido() {
        Exception ex = assertThrows(ValidationException.class, () -> controller.creaPrenotazione(
                "Mario Rossi",
                "invalid",
                LocalDate.of(2025, 3, 15),
                "1234567890",
                "Test",
                "3"
        ));
        assertEquals("Formato orario non valido. Usa 'HH:mm'.", ex.getMessage());
    }

    @Test
    void testCreaPrenotazioneConDataNulla() {
        Exception ex = assertThrows(ValidationException.class, () -> controller.creaPrenotazione(
                "Mario Rossi",
                "20:00",
                null,
                "1234567890",
                "Test",
                "3"
        ));
        assertEquals("Seleziona una data valida.", ex.getMessage());
    }

    @Test
    void testCreaPrenotazioneConTelefonoNonValido() {
        Exception ex = assertThrows(ValidationException.class, () -> controller.creaPrenotazione(
                "Mario Rossi",
                "20:00",
                LocalDate.of(2025, 3, 15),
                "invalid_123",
                "Test",
                "3"
        ));
        assertEquals("Il telefono deve contenere solo numeri.", ex.getMessage());
    }

    @Test
    void testCreaPrenotazioneConCopertiNegativi() {
        Exception ex = assertThrows(ValidationException.class, () -> controller.creaPrenotazione(
                "Mario Rossi",
                "20:00",
                LocalDate.of(2025, 3, 15),
                "1234567890",
                "Test",
                "-1"
        ));
        assertEquals("I coperti devono essere maggiori di 0.", ex.getMessage());
    }

    @Test
    void testEliminaPrenotazioneNonEsistente() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.eliminaPrenotazione(999));
        assertEquals("La prenotazione con ID 999 non esiste.", ex.getMessage());
    }
}