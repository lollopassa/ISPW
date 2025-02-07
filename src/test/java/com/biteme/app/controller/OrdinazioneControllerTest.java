package com.biteme.app.controller;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.model.Ordinazione;
import com.biteme.app.model.StatoOrdine;
import com.biteme.app.model.TipoOrdine;
import com.biteme.app.persistence.inmemory.InMemoryOrdinazioneDao;
import com.biteme.app.persistence.inmemory.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdinazioneControllerTest {

    private OrdinazioneController controller;
    private InMemoryOrdinazioneDao inMemoryDao;

    @BeforeEach
    void setUp() throws Exception {
        // Pulisce lo storage condiviso per garantire test isolati
        Storage.getInstance().getOrdinazioni().clear();

        // Inizializza il controller
        controller = new OrdinazioneController();

        // Crea una nuova InMemoryOrdinazioneDao per mockare la persistenza
        inMemoryDao = new InMemoryOrdinazioneDao();

        // Usa reflection per iniettare il DAO in-memory nella classe OrdinazioneController
        Field daoField = OrdinazioneController.class.getDeclaredField("ordinazioneDao");
        daoField.setAccessible(true);
        daoField.set(controller, inMemoryDao);

        // Inietta anche il OrdineController (mockato o reale, a seconda delle esigenze)
        Field ordineControllerField = OrdinazioneController.class.getDeclaredField("ordineController");
        ordineControllerField.setAccessible(true);
        ordineControllerField.set(controller, new OrdineController());
    }

    @Test
    void testCreaOrdine() {
        OrdinazioneBean ordinazioneBean = new OrdinazioneBean();
        ordinazioneBean.setNome("Mario Rossi");
        ordinazioneBean.setNumeroClienti("4"); // Passa come Stringa
        ordinazioneBean.setTipoOrdine("Al Tavolo");
        ordinazioneBean.setInfoTavolo("Tavolo 5");
        ordinazioneBean.setOrarioCreazione("20:00"); // Passa l'orario come Stringa

        controller.creaOrdine(ordinazioneBean);

        List<Ordinazione> ordinazioni = Storage.getInstance().getOrdinazioni();
        assertEquals(1, ordinazioni.size());

        Ordinazione o = ordinazioni.get(0);

        assertTrue(o.getId() > 0); // Confronto con un numero intero è valido
        assertEquals("Mario Rossi", o.getNomeCliente()); // Confronto diretto tra stringhe
        assertEquals("4", String.valueOf(o.getNumeroClienti())); // Converte l'int atteso in una stringa per il confronto
        assertEquals(TipoOrdine.AL_TAVOLO, o.getTipoOrdine()); // Valore enum è confrontato con un altro valore enum
        assertEquals("Tavolo 5", o.getInfoTavolo()); // Confronto diretto di stringhe
        assertEquals(StatoOrdine.NUOVO, o.getStatoOrdine()); // Valore enum correttamente confrontato
        assertEquals("20:00", o.getOrarioCreazione()); // Confronto coerente con stringhe
    }

    @Test
    void testGetOrdini() {
        Ordinazione ordinazione = new Ordinazione(
                1,
                "Anna Verdi",
                "3", // Numero clienti come Stringa
                TipoOrdine.ASPORTO,
                "Nessuna",
                StatoOrdine.NUOVO,
                "19:00" // Orario come Stringa
        );
        inMemoryDao.store(ordinazione);

        List<OrdinazioneBean> ordinazioni = controller.getOrdini();
        assertNotNull(ordinazioni);
        assertEquals(1, ordinazioni.size());

        OrdinazioneBean bean = ordinazioni.get(0);

        assertEquals(1, bean.getId());
        assertEquals("Anna Verdi", bean.getNome());
        assertEquals("3", bean.getNumeroClienti()); // Modifica per confrontare stringhe
        assertEquals("Asporto", bean.getTipoOrdine());
        assertEquals("Nessuna", bean.getInfoTavolo());
        // Modifica: confronta "Nuovo" (con la prima lettera maiuscola) anziché "NUOVO"
        assertEquals("Nuovo", bean.getStatoOrdine());
        assertEquals("19:00", bean.getOrarioCreazione()); // Modifica per confrontare stringhe
    }

    @Test
    void testEliminaOrdinazione() {
        Ordinazione ordinazione = new Ordinazione(
                1,
                "Mario Rossi",
                "4", // Numero clienti come Stringa
                TipoOrdine.AL_TAVOLO,
                "Tavolo 5",
                StatoOrdine.NUOVO,
                "20:00" // Orario come Stringa
        );
        inMemoryDao.store(ordinazione);
        int storedId = ordinazione.getId();

        controller.eliminaOrdinazione(storedId);

        assertFalse(inMemoryDao.exists(storedId));
    }

    @Test
    void testEliminaOrdinazioneNonEsistente() {
        Exception ex = assertThrows(OrdinazioneException.class, () -> controller.eliminaOrdinazione(999));
        assertEquals("L'ordinazione con ID 999 non esiste.", ex.getMessage());
    }

    @Test
    void testAggiornaStatoOrdinazione() {
        Ordinazione ordinazione = new Ordinazione(
                1,
                "Giovanna Bianchi",
                "2", // Numero clienti come Stringa
                TipoOrdine.ASPORTO,
                "Nessuna",
                StatoOrdine.NUOVO,
                "18:00" // Orario come Stringa
        );
        inMemoryDao.store(ordinazione);
        int storedId = ordinazione.getId();

        // Usa uno stato valido, ad esempio IN_CORSO
        controller.aggiornaStatoOrdinazione(storedId, StatoOrdine.IN_CORSO);

        Ordinazione updatedOrdinazione = inMemoryDao.load(storedId).orElse(null);
        assertNotNull(updatedOrdinazione);
        assertEquals(StatoOrdine.IN_CORSO, updatedOrdinazione.getStatoOrdine());
    }

    @Test
    void testAggiornaStatoOrdinazioneNonEsistente() {
        // Modifica: ora ci aspettiamo che non venga lanciata alcuna eccezione
        assertDoesNotThrow(() ->
                controller.aggiornaStatoOrdinazione(999, StatoOrdine.IN_CORSO) // Usa uno stato valido
        );
        // Inoltre, verifichiamo che l'ordinazione con ID 999 non esista
        assertFalse(inMemoryDao.exists(999));
    }

    @Test
    void testIsValidTime() {
        assertTrue(controller.isValidTime("12:30"));
        assertFalse(controller.isValidTime("25:00")); // Ora non valida
        assertFalse(controller.isValidTime("12:60")); // Minuti non validi
        assertFalse(controller.isValidTime("invalid")); // Formato non valido
    }
}
