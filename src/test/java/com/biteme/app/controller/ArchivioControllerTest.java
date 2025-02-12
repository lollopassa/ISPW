package com.biteme.app.controller;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.model.Archivio;
import com.biteme.app.persistence.ArchivioDao;
import com.biteme.app.persistence.inmemory.Storage;
import com.biteme.app.persistence.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ArchivioControllerTest {

    private ArchivioController controller;
    private ArchivioDao archivioDao;
    // Lista per tracciare gli idOrdine inseriti durante i test (in modo che possano essere eliminati in tearDown)
    private final List<Integer> createdArchivioIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        // Inizializza il controller
        controller = new ArchivioController();

        // Recupera il DAO tramite la configurazione (funziona per in-memory, txt o database)
        archivioDao = Configuration.getPersistenceProvider().getDaoFactory().getArchivioDao();

        // Inietta il DAO nel controller usando reflection
        Field daoField = ArchivioController.class.getDeclaredField("archivioDao");
        daoField.setAccessible(true);
        daoField.set(controller, archivioDao);
    }

    @AfterEach
    void tearDown() {
        // Se si usa la persistenza in-memory, si svuota lo storage
        if (Configuration.getPersistenceProvider().getDaoFactory()
                .getClass().getName().contains("InMemoryDaoFactory")) {
            Storage.getInstance().getArchivi().clear();
        } else {
            // Altrimenti, per ciascun id registrato, se l'archivio esiste viene eliminato
            for (Integer id : createdArchivioIds) {
                if (archivioDao.exists(id)) {
                    archivioDao.delete(id);
                }
            }
        }
        createdArchivioIds.clear();
    }

    @Test
    void testArchiviaOrdine() {
        int newId = 1000; // ID univoco per il test

        ArchivioBean bean = new ArchivioBean();
        bean.setIdOrdine(newId);
        bean.setProdotti(Arrays.asList("Pizza", "Pasta"));
        bean.setQuantita(Arrays.asList(2, 3));
        bean.setTotale(new BigDecimal("30.50"));
        bean.setDataArchiviazione(LocalDateTime.now());

        // Archivia l'ordine tramite il controller
        controller.archiviaOrdine(bean);

        // Recupera il record creato filtrando per idOrdine
        List<Archivio> archivi = archivioDao.getAll().stream()
                .filter(a -> a.getIdOrdine() == newId)
                .toList();
        assertEquals(1, archivi.size(), "Dovrebbe esserci un solo archivio per l'idOrdine specificato.");

        Archivio archivio = archivi.get(0);
        // Registra l'id per poterlo eliminare in tearDown()
        registerInsertedArchivio(archivio);

        // Verifica il contenuto
        assertEquals(Arrays.asList("Pizza", "Pasta"), archivio.getProdotti());
        assertEquals(Arrays.asList(2, 3), archivio.getQuantita());
        assertEquals(new BigDecimal("30.50"), archivio.getTotale());
        assertNotNull(archivio.getDataArchiviazione());
    }

    @Test
    void testPiattiPiuOrdinati() {
        LocalDateTime now = LocalDateTime.now();
        int id1 = 2000;
        int id2 = 2001;

        // Inseriamo il primo archivio
        Archivio archivio1 = new Archivio();
        archivio1.setIdOrdine(id1);
        archivio1.setProdotti(Arrays.asList("Pizza", "Pasta"));
        archivio1.setQuantita(Arrays.asList(2, 3));
        archivio1.setTotale(new BigDecimal("25.00"));
        archivio1.setDataArchiviazione(now.minusDays(1));
        archivioDao.store(archivio1);
        registerInsertedArchivio(archivio1);

        // Inseriamo il secondo archivio
        Archivio archivio2 = new Archivio();
        archivio2.setIdOrdine(id2);
        archivio2.setProdotti(Arrays.asList("Pizza", "Salad"));
        archivio2.setQuantita(Arrays.asList(1, 4));
        archivio2.setTotale(new BigDecimal("30.00"));
        archivio2.setDataArchiviazione(now.minusDays(2));
        archivioDao.store(archivio2);
        registerInsertedArchivio(archivio2);

        // Otteniamo la mappa dei piatti più ordinati nel periodo specificato
        Map<String, Number> result = controller.piattiPiuOrdinati(now.minusDays(3), now);

        // Attesi:
        // "Pizza": 2+1 = 3, "Pasta": 3, "Salad": 4.
        // La mappa è ordinata in ordine decrescente rispetto al numero di ordinazioni:
        Iterator<Map.Entry<String, Number>> it = result.entrySet().iterator();
        Map.Entry<String, Number> firstEntry = it.next();
        assertEquals("Salad", firstEntry.getKey(), "Il piatto più ordinato dovrebbe essere Salad.");
        assertEquals(4, firstEntry.getValue().intValue(), "Il numero di ordinazioni per Salad dovrebbe essere 4.");

        assertEquals(3, result.get("Pizza").intValue(), "Il numero di ordinazioni per Pizza dovrebbe essere 3.");
        assertEquals(3, result.get("Pasta").intValue(), "Il numero di ordinazioni per Pasta dovrebbe essere 3.");
    }

    @Test
    void testPiattiPiuOrdinatiPerPeriodoValid() {
        LocalDateTime now = LocalDateTime.now();
        int newId = 3000;

        Archivio archivio = new Archivio();
        archivio.setIdOrdine(newId);
        archivio.setProdotti(Collections.singletonList("Burger"));
        archivio.setQuantita(Collections.singletonList(5));
        archivio.setTotale(new BigDecimal("50.00"));
        archivio.setDataArchiviazione(now.minusDays(2));
        archivioDao.store(archivio);
        registerInsertedArchivio(archivio);

        // Verifica per i diversi periodi validi
        Map<String, Number> settimana = controller.piattiPiuOrdinatiPerPeriodo("settimana");
        assertTrue(settimana.containsKey("Burger"), "La mappa dovrebbe contenere 'Burger' per il periodo settimana.");
        assertEquals(5, settimana.get("Burger").intValue(), "Il numero di ordinazioni per Burger dovrebbe essere 5.");

        Map<String, Number> mese = controller.piattiPiuOrdinatiPerPeriodo("mese");
        assertTrue(mese.containsKey("Burger"), "La mappa dovrebbe contenere 'Burger' per il periodo mese.");
        assertEquals(5, mese.get("Burger").intValue(), "Il numero di ordinazioni per Burger dovrebbe essere 5.");

        Map<String, Number> trimestre = controller.piattiPiuOrdinatiPerPeriodo("trimestre");
        assertTrue(trimestre.containsKey("Burger"), "La mappa dovrebbe contenere 'Burger' per il periodo trimestre.");
        assertEquals(5, trimestre.get("Burger").intValue(), "Il numero di ordinazioni per Burger dovrebbe essere 5.");
    }

    @Test
    void testPiattiPiuOrdinatiPerPeriodoInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                controller.piattiPiuOrdinatiPerPeriodo("annuale")
        );
        assertEquals("Periodo non valido. Deve essere 'settimana', 'mese' o 'trimestre'.", exception.getMessage());
    }

    public Map<String, Number> guadagniPerPeriodo(String periodo) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;
        switch (periodo.toLowerCase()) {
            case "settimana":
                start = end.minusWeeks(1);
                break;
            case "mese":
                start = end.minusMonths(1);
                break;
            case "trimestre":
                start = end.minusMonths(3);
                break;
            default:
                throw new IllegalArgumentException("Periodo non valido. Deve essere 'settimana', 'mese' o 'trimestre'.");
        }

        // Utilizziamo una mappa ordinata per mantenere l'ordine di inserimento (opzionale)
        Map<String, BigDecimal> guadagni = new LinkedHashMap<>();

        // Itera su tutti gli archivi
        for (Archivio archivio : archivioDao.getAll()) {
            // Considera solo gli archivi nel periodo richiesto
            if (archivio.getDataArchiviazione().isBefore(start) || archivio.getDataArchiviazione().isAfter(end)) {
                continue;
            }

            List<String> prodotti = archivio.getProdotti();
            List<Integer> quantita = archivio.getQuantita();
            BigDecimal totale = archivio.getTotale();

            // Per ogni prodotto dell'ordine
            for (int i = 0; i < prodotti.size(); i++) {
                String prodotto = prodotti.get(i);
                BigDecimal value;
                if (i == 0) {
                    // Per il primo prodotto usiamo la quantità (convertita in BigDecimal)
                    value = new BigDecimal(quantita.get(i));
                } else {
                    // Per gli altri usiamo il totale dell'ordine
                    value = totale;
                }
                // Sommiamo il valore all'eventuale guadagno già presente per quel prodotto
                guadagni.put(prodotto, guadagni.getOrDefault(prodotto, BigDecimal.ZERO).add(value));
            }
        }

        return new LinkedHashMap<>(guadagni);
    }

    @Test
    void testGuadagniPerGiorno() {
        LocalDateTime monday = LocalDateTime.of(2025, 1, 6, 12, 0);   // Lunedì
        LocalDateTime tuesday = LocalDateTime.of(2025, 1, 7, 12, 0);  // Martedì
        int id1 = 5000, id2 = 5001, id3 = 5002;

        Archivio archivio1 = new Archivio();
        archivio1.setIdOrdine(id1);
        archivio1.setProdotti(Collections.singletonList("Steak"));
        archivio1.setQuantita(Collections.singletonList(2));
        archivio1.setTotale(new BigDecimal("80.00"));
        archivio1.setDataArchiviazione(monday);
        archivioDao.store(archivio1);
        registerInsertedArchivio(archivio1);

        Archivio archivio2 = new Archivio();
        archivio2.setIdOrdine(id2);
        archivio2.setProdotti(Collections.singletonList("Steak"));
        archivio2.setQuantita(Collections.singletonList(1));
        archivio2.setTotale(new BigDecimal("40.00"));
        archivio2.setDataArchiviazione(monday);
        archivioDao.store(archivio2);
        registerInsertedArchivio(archivio2);

        Archivio archivio3 = new Archivio();
        archivio3.setIdOrdine(id3);
        archivio3.setProdotti(Collections.singletonList("Fish"));
        archivio3.setQuantita(Collections.singletonList(3));
        archivio3.setTotale(new BigDecimal("90.00"));
        archivio3.setDataArchiviazione(tuesday);
        archivioDao.store(archivio3);
        registerInsertedArchivio(archivio3);

        Map<String, Number> guadagniGiorno = controller.guadagniPerGiorno("trimestre");

        // Attesi:
        // Lunedì ("Lun"): 80 + 40 = 120.0, Martedì ("Mar"): 90.0,
        // per gli altri giorni: 0.0
        assertEquals(120.0, guadagniGiorno.get("Lun").doubleValue(), 0.001, "Il guadagno per Lunedi dovrebbe essere 120.0.");
        assertEquals(90.0, guadagniGiorno.get("Mar").doubleValue(), 0.001, "Il guadagno per Martedi dovrebbe essere 90.0.");
        assertEquals(0.0, guadagniGiorno.get("Mer").doubleValue(), 0.001, "Il guadagno per Mercoledi dovrebbe essere 0.0.");
        assertEquals(0.0, guadagniGiorno.get("Gio").doubleValue(), 0.001, "Il guadagno per Giovedi dovrebbe essere 0.0.");
        assertEquals(0.0, guadagniGiorno.get("Ven").doubleValue(), 0.001, "Il guadagno per Venerdi dovrebbe essere 0.0.");
        assertEquals(0.0, guadagniGiorno.get("Sab").doubleValue(), 0.001, "Il guadagno per Sabato dovrebbe essere 0.0.");
        assertEquals(0.0, guadagniGiorno.get("Dom").doubleValue(), 0.001, "Il guadagno per Domenica dovrebbe essere 0.0.");
    }

    @Test
    void testGuadagniPerGiornoInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                controller.guadagniPerGiorno("annuale")
        );
        assertEquals("Periodo non valido. Deve essere 'settimana', 'mese' o 'trimestre'.", exception.getMessage());
    }

    // Metodo helper per registrare gli ID degli archivi inseriti durante i test
    private void registerInsertedArchivio(Archivio archivio) {
        createdArchivioIds.add(archivio.getIdOrdine());
    }
}
