package com.biteme.app.controller;

import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.exception.ValidationException;
import com.biteme.app.entities.Prenotazione;
import com.biteme.app.persistence.PrenotazioneDao;
import com.biteme.app.persistence.inmemory.Storage; // Utilizzato solo se la persistenza è in memory
import com.biteme.app.persistence.Configuration;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@author Kevin Hoxha

class PrenotazioneControllerTest {

    private PrenotazioneController controller;
    private PrenotazioneDao prenotazioneDao;
    private final List<Integer> createdPrenotazioniIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        // Inizializza il controller
        controller = new PrenotazioneController();

        // Recupera il DAO generico per le prenotazioni tramite la configurazione
        prenotazioneDao = Configuration.getPersistenceProvider().getDaoFactory().getPrenotazioneDao();

        // Usa reflection per iniettare il DAO nella classe PrenotazioneController
        Field daoField = PrenotazioneController.class.getDeclaredField("prenotazioneDao");
        daoField.setAccessible(true);
        daoField.set(controller, prenotazioneDao);
    }

    @AfterEach
    void tearDown() {
        if (Configuration.getPersistenceProvider().getDaoFactory()
                instanceof com.biteme.app.persistence.inmemory.InMemoryDaoFactory) {
            Storage.getInstance().getPrenotazioni().clear();
        } else {
            for (Integer id : createdPrenotazioniIds) {
                if (prenotazioneDao.exists(id)) {
                    prenotazioneDao.delete(id);
                }
            }
        }
        createdPrenotazioniIds.clear();
    }

    private void removeExistingPrenotazione(String nome, LocalDate data, String orario) {
        List<Prenotazione> prenotazioni = prenotazioneDao.getByData(data);
        LocalTime time = LocalTime.parse(orario);
        prenotazioni.stream()
                .filter(p -> nome.equals(p.getNomeCliente()) && p.getOrario().equals(time))
                .forEach(p -> {
                    prenotazioneDao.delete(p.getId());
                    // Rimuovo anche dall'elenco degli ID creati (se presente)
                    createdPrenotazioniIds.remove(Integer.valueOf(p.getId()));
                });
    }

    @Test
    void testCreaPrenotazione() {
        removeExistingPrenotazione("Mario Rossi", LocalDate.of(2025, 4, 15), "20:00");

        controller.creaPrenotazione(
                "Mario Rossi",
                "20:00",
                LocalDate.of(2025, 4, 15),
                "",
                "Cena di compleanno",
                "3"
        );

        List<Prenotazione> prenotazioni = prenotazioneDao.getByData(LocalDate.of(2025, 4, 15));
        List<Prenotazione> filtered = prenotazioni.stream()
                .filter(p -> "Mario Rossi".equals(p.getNomeCliente()))
                .toList();
        assertEquals(1, filtered.size());

        Prenotazione p = filtered.get(0);
        createdPrenotazioniIds.add(p.getId());

        assertTrue(p.getId() > 0);
        assertEquals("Mario Rossi", p.getNomeCliente());
        assertEquals(LocalDate.of(2025, 4, 15), p.getData());
        assertEquals(LocalTime.of(20, 0), p.getOrario());
        assertEquals("Cena di compleanno", p.getNote());
        assertEquals("", p.getEmail());
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
                "anna.verdi@example.com",
                4
        );
        prenotazioneDao.store(initPrenotazione);
        int storedId = initPrenotazione.getId();
        createdPrenotazioniIds.add(storedId);

        PrenotazioneBean updatedBean = controller.modificaPrenotazione(
                storedId,
                "Anna Verdi Modificata",
                "20:30",
                LocalDate.of(2025, 5, 25),
                "",
                "Cena privata",
                "5"
        );

        assertNotNull(updatedBean);
        assertEquals(storedId, updatedBean.getId());
        assertEquals("Anna Verdi Modificata", updatedBean.getNomeCliente());
        assertEquals(LocalTime.of(20, 30), updatedBean.getOrario());
        assertEquals(LocalDate.of(2025, 5, 25), updatedBean.getData());
        assertEquals("Cena privata", updatedBean.getNote());
        assertEquals("", updatedBean.getEmail());
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
                "mario.rossi@example.com",
                4
        );
        prenotazioneDao.store(prenotazione);
        int storedId = prenotazione.getId();
        // Anche se verrà eliminata nel test, segnalo l'ID per eventuale cleanup
        createdPrenotazioniIds.add(storedId);

        controller.eliminaPrenotazione(storedId);

        assertFalse(prenotazioneDao.exists(storedId));
        // Rimuovo l'ID dalla lista perché è già stato eliminato
        createdPrenotazioniIds.remove(Integer.valueOf(storedId));
    }

    @Test
    void testGetPrenotazioniByData() {
        Prenotazione p = new Prenotazione(
                0,
                "Giovanna Bianchi",
                LocalTime.of(19, 30),
                LocalDate.of(2025, 7, 20),
                "Festa privata",
                "giovanna.bianchi@example.com",
                6
        );
        prenotazioneDao.store(p);
        int id = p.getId();
        createdPrenotazioniIds.add(id);

        List<PrenotazioneBean> prenotazioni = controller.getPrenotazioniByData(LocalDate.of(2025, 7, 20));
        assertNotNull(prenotazioni);
        List<PrenotazioneBean> filtered = prenotazioni.stream()
                .filter(bean -> "Giovanna Bianchi".equals(bean.getNomeCliente()))
                .toList();
        assertEquals(1, filtered.size());

        PrenotazioneBean bean = filtered.get(0);

        assertEquals("Giovanna Bianchi", bean.getNomeCliente());
        assertEquals(LocalDate.of(2025, 7, 20), bean.getData());
        assertEquals(LocalTime.of(19, 30), bean.getOrario());
        assertEquals("Festa privata", bean.getNote());
        assertEquals("giovanna.bianchi@example.com", bean.getEmail());
        assertEquals(6, bean.getCoperti());
    }

    @Test
    void testCreaPrenotazioneConNomeVuoto() {
        assertThrowsValidationException("Il nome del cliente non può essere vuoto.",
                "   ", "20:00", LocalDate.of(2025, 3, 15),
                "mario.rossi@example.com", "Test", "3");
    }

    @Test
    void testCreaPrenotazioneConOrarioNonValido() {
        assertThrowsValidationException("Formato orario non valido. Usa 'HH:mm'.",
                "Mario Rossi", "invalid", LocalDate.of(2025, 3, 15),
                "mario.rossi@example.com", "Test", "3");
    }

    @Test
    void testCreaPrenotazioneConDataNulla() {
        assertThrowsValidationException("Seleziona una data valida.",
                "Mario Rossi", "20:00", null,
                "mario.rossi@example.com", "Test", "3");
    }

    @Test
    void testCreaPrenotazioneConEmailNonValida() {
        assertThrowsValidationException("Formato email non valido.",
                "Mario Rossi", "20:00", LocalDate.of(2025, 3, 15),
                "invalid_123", "Test", "3");
    }

    @Test
    void testCreaPrenotazioneConCopertiNegativi() {
        assertThrowsValidationException("I coperti devono essere maggiori di 0.",
                "Mario Rossi", "20:00", LocalDate.of(2025, 3, 15),
                "mario.rossi@example.com", "Test", "-1");
    }

    @Test
    void testEliminaPrenotazioneNonEsistente() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.eliminaPrenotazione(999));
        assertEquals("La prenotazione con ID 999 non esiste.", ex.getMessage());
    }

    // Metodo helper per centralizzare la gestione delle eccezioni di validazione
    private void assertThrowsValidationException(String expectedMessage, String nome, String orario, LocalDate data, String email, String note, String coperti) {
        Exception ex = assertThrows(ValidationException.class, () -> {
            controller.creaPrenotazione(nome, orario, data, email, note, coperti);
        });
        assertEquals(expectedMessage, ex.getMessage());
    }
}
