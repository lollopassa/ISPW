package com.biteme.app.controller;

import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.entities.Prenotazione;
import com.biteme.app.persistence.PrenotazioneDao;
import com.biteme.app.persistence.inmemory.Storage;
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

        controller = new PrenotazioneController();


        prenotazioneDao = Configuration.getPersistenceProvider().getDaoFactory().getPrenotazioneDao();


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

                    createdPrenotazioniIds.remove(Integer.valueOf(p.getId()));
                });
    }

    @Test
    void testCreaPrenotazione() {
        removeExistingPrenotazione("Mario Rossi", LocalDate.of(2025, 4, 15), "20:00");


        PrenotazioneBean bean = new PrenotazioneBean();
        bean.setNomeCliente("Mario Rossi");
        bean.setOrarioStr("20:00");
        bean.setData(LocalDate.of(2025, 4, 15));
        bean.setEmail("");
        bean.setNote("Cena di compleanno");
        bean.setCopertiStr("3");
        bean.validate();
        controller.creaPrenotazione(bean);

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
                "",
                4
        );
        prenotazioneDao.create(initPrenotazione);
        int storedId = initPrenotazione.getId();
        createdPrenotazioniIds.add(storedId);


        PrenotazioneBean modBean = new PrenotazioneBean();
        modBean.setId(storedId);
        modBean.setNomeCliente("Anna Verdi Modificata");
        modBean.setOrarioStr("20:30");
        modBean.setData(LocalDate.of(2025, 5, 25));
        modBean.setEmail("");
        modBean.setNote("Cena privata");
        modBean.setCopertiStr("5");
        modBean.validate();


        PrenotazioneBean updatedBean = controller.modificaPrenotazione(modBean);

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
        prenotazioneDao.create(prenotazione);
        int storedId = prenotazione.getId();
        createdPrenotazioniIds.add(storedId);

        controller.eliminaPrenotazione(storedId);

        assertFalse(prenotazioneDao.exists(storedId));

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
        prenotazioneDao.create(p);
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
    void testEliminaPrenotazioneNonEsistente() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.eliminaPrenotazione(999));
        assertEquals("La prenotazione con ID 999 non esiste.", ex.getMessage());
    }

}
