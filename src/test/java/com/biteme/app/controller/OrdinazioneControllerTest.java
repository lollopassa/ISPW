package com.biteme.app.controller;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.entities.Ordinazione;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.entities.TipoOrdinazione;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.persistence.inmemory.Storage; // Utilizzato solo per il cleanup in modalità in memory
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
        // Inizializza il controller
        controller = new OrdinazioneController();

        // Recupera il DAO generico per Ordinazioni tramite la configurazione
        ordinazioneDao = Configuration.getPersistenceProvider().getDaoFactory().getOrdinazioneDao();

        // Usa reflection per iniettare il DAO nella classe OrdinazioneController
        Field daoField = OrdinazioneController.class.getDeclaredField("ordinazioneDao");
        daoField.setAccessible(true);
        daoField.set(controller, ordinazioneDao);

        // Inietta anche il controller per l'ordine, se richiesto
        Field ordineControllerField = OrdinazioneController.class.getDeclaredField("ordineController");
        ordineControllerField.setAccessible(true);
        ordineControllerField.set(controller, new OrdineController());
    }

    @AfterEach
    void tearDown() {
        // Se la persistenza è in memory, pulisci lo storage per garantire test isolati
        if (Configuration.getPersistenceProvider().getDaoFactory()
                instanceof com.biteme.app.persistence.inmemory.InMemoryDaoFactory) {
            Storage.getInstance().getOrdinazioni().clear();
        } else {
            // Per la modalità database, rimuovo soltanto i record creati per i test
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
    void testCreaOrdine() {
        // Rimuove eventuali ordinazioni già presenti con gli stessi dati
        removeExistingOrdine("Mario Rossi", "4", "20:00");

        OrdinazioneBean ordinazioneBean = new OrdinazioneBean();
        ordinazioneBean.setNome("Mario Rossi");
        ordinazioneBean.setNumeroClienti("4"); // Passato come Stringa
        ordinazioneBean.setTipoOrdine("Al Tavolo");
        ordinazioneBean.setInfoTavolo("5");
        ordinazioneBean.setOrarioCreazione("20:00"); // Orario come Stringa

        controller.creaOrdine(ordinazioneBean);

        // Recupera tutte le ordinazioni salvate
        List<Ordinazione> ordinazioni = ordinazioneDao.getAll();

        // Filtra per trovare l'ordine appena creato in base ai dati specifici
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

        assertTrue(o.getId() > 0); // L'ID deve essere maggiore di 0
        assertEquals("Mario Rossi", o.getNomeCliente());
        // Converte il numero di clienti (se memorizzato come int) in stringa per il confronto
        assertEquals("4", String.valueOf(o.getNumeroClienti()));
        assertEquals(TipoOrdinazione.AL_TAVOLO, o.getTipoOrdine());
        assertEquals("5", o.getInfoTavolo());
        assertEquals(StatoOrdinazione.NUOVO, o.getStatoOrdine());
        assertEquals("20:00", o.getOrarioCreazione());
    }

    @Test
    void testGetOrdini() {
        // Rimuove eventuali ordinazioni duplicate per "Anna Verdi"
        removeExistingOrdine("Anna Verdi", "3", "19:00");

        // Crea e salva un'ordinazione di esempio
        Ordinazione ordinazione = new Ordinazione(
                1,              // L'ID verrà ignorato e sovrascritto dall'auto-increment
                "Anna Verdi",
                "3",          // Numero clienti come Stringa
                TipoOrdinazione.ASPORTO,
                "Nessuna",
                StatoOrdinazione.NUOVO,
                "19:00"       // Orario come Stringa
        );
        ordinazioneDao.store(ordinazione);

        List<OrdinazioneBean> ordinazioni = controller.getOrdini();
        assertNotNull(ordinazioni);

        // Filtra per ottenere solamente l'ordine con i dati di Anna Verdi
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

        // Invece di verificare che l'ID sia 1, verifichiamo che sia uguale all'ID dell'ordinazione salvata
        assertEquals(ordinazione.getId(), bean.getId());

        // Altre asserzioni rimangono invariate
        assertEquals("Anna Verdi", bean.getNome());
        assertEquals("3", bean.getNumeroClienti());
        assertEquals("Asporto", bean.getTipoOrdine());
        assertEquals("Nessuna", bean.getInfoTavolo());
        assertEquals("Nuovo", bean.getStatoOrdine());
        assertEquals("19:00", bean.getOrarioCreazione());
    }

    @Test
    void testEliminaOrdinazione() {
        // Rimuove eventuali ordinazioni duplicate per "Mario Rossi"
        removeExistingOrdine("Mario Rossi", "4", "20:00");

        Ordinazione ordinazione = new Ordinazione(
                1,
                "Mario Rossi",
                "4",       // Numero clienti come Stringa
                TipoOrdinazione.AL_TAVOLO,
                "Tavolo 5",
                StatoOrdinazione.NUOVO,
                "20:00"    // Orario come Stringa
        );
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
    void testAggiornaStatoOrdinazione() {
        // Rimuove eventuali ordinazioni duplicate per "Giovanna Bianchi"
        removeExistingOrdine("Giovanna Bianchi", "2", "18:00");

        Ordinazione ordinazione = new Ordinazione(
                1,
                "Giovanna Bianchi",
                "2",       // Numero clienti come Stringa
                TipoOrdinazione.ASPORTO,
                "Nessuna",
                StatoOrdinazione.NUOVO,
                "18:00"    // Orario come Stringa
        );
        ordinazioneDao.store(ordinazione);
        int storedId = ordinazione.getId();

        // Aggiorna lo stato a IN_CORSO
        controller.aggiornaStatoOrdinazione(storedId, StatoOrdinazione.IN_CORSO);

        Ordinazione updatedOrdinazione = ordinazioneDao.load(storedId).orElse(null);
        assertNotNull(updatedOrdinazione);
        assertEquals(StatoOrdinazione.IN_CORSO, updatedOrdinazione.getStatoOrdine());
    }

    @Test
    void testAggiornaStatoOrdinazioneNonEsistente() {
        // Verifica che, per un ID non esistente, non venga lanciata eccezione e che l'ordinazione non esista
        assertDoesNotThrow(() -> controller.aggiornaStatoOrdinazione(999, StatoOrdinazione.IN_CORSO));
        assertFalse(ordinazioneDao.exists(999));
    }

    @Test
    void testIsValidTime() {
        assertTrue(controller.isValidTime("12:30"));
        assertFalse(controller.isValidTime("25:00")); // Ora non valida
        assertFalse(controller.isValidTime("12:60")); // Minuti non validi
        assertFalse(controller.isValidTime("invalid")); // Formato non valido
    }
}
