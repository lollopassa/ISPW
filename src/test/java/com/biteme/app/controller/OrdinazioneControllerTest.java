package com.biteme.app.controller;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.entities.TipoOrdinazione;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.persistence.inmemory.Storage;
import com.biteme.app.persistence.Configuration;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@author Lorenzo Passacantilli

class OrdinazioneControllerTest {

    private OrdinazioneController controller;
    private OrdinazioneDao ordinazioneDao;

    @BeforeEach
    void setUp() throws Exception {

        controller = new OrdinazioneController();


        ordinazioneDao = Configuration.getPersistenceProvider().getDaoFactory().getOrdinazioneDao();


        Field daoField = OrdinazioneController.class.getDeclaredField("ordinazioneDao");
        daoField.setAccessible(true);
        daoField.set(controller, ordinazioneDao);


        Field ordineControllerField = OrdinazioneController.class.getDeclaredField("ordineController");
        ordineControllerField.setAccessible(true);
        ordineControllerField.set(controller, new OrdineController());
    }

    @AfterEach
    void tearDown() {

        if (Configuration.getPersistenceProvider().getDaoFactory()
                instanceof com.biteme.app.persistence.inmemory.InMemoryDaoFactory) {
            Storage.getInstance().getOrdinazioni().clear();
        } else {

            removeExistingOrdine("Mario Rossi", "4", "20:00");
            removeExistingOrdine("Anna Verdi", "3", "19:00");
            removeExistingOrdine("Giovanna Bianchi", "2", "18:00");
        }
    }

    private void removeExistingOrdine(String nome, String numeroClienti, String orarioCreazione) {
        List<Ordinazione> esistenti = ordinazioneDao.getAll();
        esistenti.stream()
                .filter(o -> nome.equals(o.getNomeCliente()) &&
                        String.valueOf(o.getNumeroClienti()).equals(numeroClienti) &&
                        orarioCreazione.equals(o.getOrarioCreazione()))
                .forEach(o -> {
                    if (ordinazioneDao.exists(o.getId())) {
                        ordinazioneDao.delete(o.getId());
                    }
                });
    }

    @Test
    void testCreaOrdine() throws Exception {

        removeExistingOrdine("Mario Rossi", "4", "20:00");

        OrdinazioneBean ordinazioneBean = new OrdinazioneBean();
        ordinazioneBean.setNome("Mario Rossi");
        ordinazioneBean.setNumeroClienti("4");
        ordinazioneBean.setTipoOrdine("Al Tavolo");
        ordinazioneBean.setInfoTavolo("5");
        ordinazioneBean.setOrarioCreazione("20:00");

        controller.creaOrdine(ordinazioneBean);


        List<Ordinazione> ordinazioni = ordinazioneDao.getAll();


        List<Ordinazione> filtered = ordinazioni.stream()
                .filter(o -> "Mario Rossi".equals(o.getNomeCliente())
                        && "4".equals(String.valueOf(o.getNumeroClienti()))
                        && TipoOrdinazione.AL_TAVOLO.equals(o.getTipoOrdine())
                        && "5".equals(o.getInfoTavolo())
                        && "20:00".equals(o.getOrarioCreazione())
                        && StatoOrdinazione.NUOVO.equals(o.getStatoOrdine()))
                .toList();

        assertEquals(1, filtered.size(), "Dovrebbe esserci esattamente un ordine creato con i dati specificati.");

        Ordinazione o = filtered.get(0);

        assertTrue(o.getId() > 0);
        assertEquals("Mario Rossi", o.getNomeCliente());
        assertEquals("4", String.valueOf(o.getNumeroClienti()));
        assertEquals(TipoOrdinazione.AL_TAVOLO, o.getTipoOrdine());
        assertEquals("5", o.getInfoTavolo());
        assertEquals(StatoOrdinazione.NUOVO, o.getStatoOrdine());
        assertEquals("20:00", o.getOrarioCreazione());
    }

    @Test
    void testGetOrdini() {

        removeExistingOrdine("Anna Verdi", "3", "19:00");


        Ordinazione ordinazione = new Ordinazione(
                1,
                "Anna Verdi",
                "3",                          TipoOrdinazione.ASPORTO,
                "Nessuna",
                StatoOrdinazione.NUOVO,
                "19:00"               );
        ordinazioneDao.store(ordinazione);

        List<OrdinazioneBean> ordinazioni = controller.getOrdini();
        assertNotNull(ordinazioni);

                List<OrdinazioneBean> filtered = ordinazioni.stream()
                .filter(b -> "Anna Verdi".equals(b.getNome())
                        && "3".equals(b.getNumeroClienti())
                        && "Asporto".equals(b.getTipoOrdine())
                        && "Nessuna".equals(b.getInfoTavolo())
                        && "Nuovo".equals(b.getStatoOrdine())
                        && "19:00".equals(b.getOrarioCreazione()))
                .toList();

        assertEquals(1, filtered.size(), "Dovrebbe esserci esattamente un ordine con i dati specificati.");

        OrdinazioneBean bean = filtered.get(0);

                assertEquals(ordinazione.getId(), bean.getId());

                assertEquals("Anna Verdi", bean.getNome());
        assertEquals("3", bean.getNumeroClienti());
        assertEquals("Asporto", bean.getTipoOrdine());
        assertEquals("Nessuna", bean.getInfoTavolo());
        assertEquals("Nuovo", bean.getStatoOrdine());
        assertEquals("19:00", bean.getOrarioCreazione());
    }

    @Test
    void testEliminaOrdinazione() throws Exception {
                removeExistingOrdine("Mario Rossi", "4", "20:00");

        Ordinazione ordinazione = new Ordinazione(
                1,
                "Mario Rossi",
                "4",                       TipoOrdinazione.AL_TAVOLO,
                "Tavolo 5",
                StatoOrdinazione.NUOVO,
                "20:00"            );
        ordinazioneDao.store(ordinazione);
        int storedId = ordinazione.getId();

        controller.eliminaOrdinazione(storedId);

        assertFalse(ordinazioneDao.exists(storedId));
    }

    @Test
    void testEliminaOrdinazioneNonEsistente() {
        Exception ex = assertThrows(OrdinazioneException.class, () -> controller.eliminaOrdinazione(999));
        assertEquals("L'ordinazione con ID 999 non esiste.", ex.getMessage());
    }

    @Test
    void testAggiornaStatoOrdinazione() throws Exception {
                removeExistingOrdine("Giovanna Bianchi", "2", "18:00");

        Ordinazione ordinazione = new Ordinazione(
                1,
                "Giovanna Bianchi",
                "2",                       TipoOrdinazione.ASPORTO,
                "Nessuna",
                StatoOrdinazione.NUOVO,
                "18:00"            );
        ordinazioneDao.store(ordinazione);
        int storedId = ordinazione.getId();

                controller.aggiornaStatoOrdinazione(storedId, StatoOrdinazione.IN_CORSO);

        Ordinazione updatedOrdinazione = ordinazioneDao.load(storedId).orElse(null);
        assertNotNull(updatedOrdinazione);
        assertEquals(StatoOrdinazione.IN_CORSO, updatedOrdinazione.getStatoOrdine());
    }

    @Test
    void testAggiornaStatoOrdinazioneNonEsistente() {
                assertDoesNotThrow(() -> controller.aggiornaStatoOrdinazione(999, StatoOrdinazione.IN_CORSO));
        assertFalse(ordinazioneDao.exists(999));
    }

    @Test
    void testIsValidTime() {
        assertTrue(controller.isValidTime("12:30"));
        assertFalse(controller.isValidTime("25:00"));         assertFalse(controller.isValidTime("12:60"));         assertFalse(controller.isValidTime("invalid"));     }
}
