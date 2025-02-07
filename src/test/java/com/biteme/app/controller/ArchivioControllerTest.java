package com.biteme.app.controller;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.model.Archivio;
import com.biteme.app.persistence.inmemory.InMemoryArchivioDao;
import com.biteme.app.persistence.inmemory.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

//@author Kevin Hoxha

class ArchivioControllerTest {

    private ArchivioController controller;
    private InMemoryArchivioDao inMemoryDao;

    @BeforeEach
    void setUp() throws Exception {
        Storage.getInstance().getArchivi().clear();
        controller = new ArchivioController();
        inMemoryDao = new InMemoryArchivioDao();
        Field daoField = ArchivioController.class.getDeclaredField("archivioDao");
        daoField.setAccessible(true);
        daoField.set(controller, inMemoryDao);
    }

    @Test
    void testArchiviaOrdine() {
        ArchivioBean bean = new ArchivioBean();
        bean.setIdOrdine(1);
        bean.setProdotti(Arrays.asList("Pizza", "Pasta"));
        bean.setQuantita(Arrays.asList(2, 3));
        bean.setTotale(new BigDecimal("30.50"));
        bean.setDataArchiviazione(LocalDateTime.now());

        controller.archiviaOrdine(bean);

        List<Archivio> archivi = inMemoryDao.getAll();
        assertEquals(1, archivi.size());

        Archivio archivio = archivi.get(0);
        assertEquals(1, archivio.getIdOrdine());
        assertEquals(Arrays.asList("Pizza", "Pasta"), archivio.getProdotti());
        assertEquals(Arrays.asList(2, 3), archivio.getQuantita());
        assertEquals(new BigDecimal("30.50"), archivio.getTotale());
        assertNotNull(archivio.getDataArchiviazione());
    }

    @Test
    void testPiattiPiuOrdinati() {
        LocalDateTime now = LocalDateTime.now();

        Archivio archivio1 = new Archivio();
        archivio1.setIdOrdine(1);
        archivio1.setProdotti(Arrays.asList("Pizza", "Pasta"));
        archivio1.setQuantita(Arrays.asList(2, 3));
        archivio1.setTotale(new BigDecimal("25.00"));
        archivio1.setDataArchiviazione(now.minusDays(1));
        inMemoryDao.store(archivio1);

        Archivio archivio2 = new Archivio();
        archivio2.setIdOrdine(2);
        archivio2.setProdotti(Arrays.asList("Pizza", "Salad"));
        archivio2.setQuantita(Arrays.asList(1, 4));
        archivio2.setTotale(new BigDecimal("30.00"));
        archivio2.setDataArchiviazione(now.minusDays(2));
        inMemoryDao.store(archivio2);
        Map<String, Number> result = controller.piattiPiuOrdinati(now.minusDays(3), now);

        // Attesi: "Pizza": 2+1=3, "Pasta": 3, "Salad": 4.
        // La mappa è ordinata in ordine decrescente (il primo elemento deve essere "Salad")
        Iterator<Map.Entry<String, Number>> it = result.entrySet().iterator();
        Map.Entry<String, Number> firstEntry = it.next();
        assertEquals("Salad", firstEntry.getKey());
        assertEquals(4, firstEntry.getValue().intValue());

        assertEquals(3, result.get("Pizza").intValue());
        assertEquals(3, result.get("Pasta").intValue());
    }

    @Test
    void testPiattiPiuOrdinatiPerPeriodoValid() {
        LocalDateTime now = LocalDateTime.now();

        Archivio archivio = new Archivio();
        archivio.setIdOrdine(1);
        archivio.setProdotti(Collections.singletonList("Burger"));
        archivio.setQuantita(Collections.singletonList(5));
        archivio.setTotale(new BigDecimal("50.00"));
        archivio.setDataArchiviazione(now.minusDays(2));
        inMemoryDao.store(archivio);

        // Verifica il funzionamento per ciascun periodo valido
        Map<String, Number> settimana = controller.piattiPiuOrdinatiPerPeriodo("settimana");
        assertTrue(settimana.containsKey("Burger"));
        assertEquals(5, settimana.get("Burger").intValue());

        Map<String, Number> mese = controller.piattiPiuOrdinatiPerPeriodo("mese");
        assertTrue(mese.containsKey("Burger"));
        assertEquals(5, mese.get("Burger").intValue());

        Map<String, Number> trimestre = controller.piattiPiuOrdinatiPerPeriodo("trimestre");
        assertTrue(trimestre.containsKey("Burger"));
        assertEquals(5, trimestre.get("Burger").intValue());
    }

    @Test
    void testPiattiPiuOrdinatiPerPeriodoInvalid() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                controller.piattiPiuOrdinatiPerPeriodo("annuale")
        );
        assertEquals("Periodo non valido. Deve essere 'settimana', 'mese' o 'trimestre'.", ex.getMessage());
    }

    @Test
    void testGuadagniPerPeriodo() {
        LocalDateTime now = LocalDateTime.now();

        // Crea due Archivio con prodotti e totali
        Archivio archivio1 = new Archivio();
        archivio1.setIdOrdine(1);
        archivio1.setProdotti(Arrays.asList("Sushi", "Ramen"));
        archivio1.setQuantita(Arrays.asList(2, 3));
        archivio1.setTotale(new BigDecimal("40.00"));
        archivio1.setDataArchiviazione(now.minusDays(1));
        inMemoryDao.store(archivio1);

        Archivio archivio2 = new Archivio();
        archivio2.setIdOrdine(2);
        archivio2.setProdotti(Arrays.asList("Sushi", "Tempura"));
        archivio2.setQuantita(Arrays.asList(1, 2));
        archivio2.setTotale(new BigDecimal("60.00"));
        archivio2.setDataArchiviazione(now.minusDays(2));
        inMemoryDao.store(archivio2);

        // Secondo l'implementazione, per ogni prodotto viene sommato l'intero totale dell'ordine.
        // Attesi:
        // "Sushi": 40.00 + 60.00 = 100.00, "Ramen": 40.00, "Tempura": 60.00
        Map<String, Number> guadagni = controller.guadagniPerPeriodo("mese");
        assertEquals(3, guadagni.size());
        assertEquals(100.00, guadagni.get("Sushi").doubleValue(), 0.001);
        assertEquals(40.00, guadagni.get("Ramen").doubleValue(), 0.001);
        assertEquals(60.00, guadagni.get("Tempura").doubleValue(), 0.001);
    }

    @Test
    void testGuadagniPerGiorno() {
        // Per testare i guadagni per giorno, creiamo Archivio con date specifiche (ad es. lunedì e martedì)
        LocalDateTime monday = LocalDateTime.of(2025, 1, 6, 12, 0);   // Lunedì
        LocalDateTime tuesday = LocalDateTime.of(2025, 1, 7, 12, 0);  // Martedì

        Archivio archivio1 = new Archivio();
        archivio1.setIdOrdine(1);
        archivio1.setProdotti(Collections.singletonList("Steak"));
        archivio1.setQuantita(Collections.singletonList(2));
        archivio1.setTotale(new BigDecimal("80.00"));
        archivio1.setDataArchiviazione(monday);
        inMemoryDao.store(archivio1);

        Archivio archivio2 = new Archivio();
        archivio2.setIdOrdine(2);
        archivio2.setProdotti(Collections.singletonList("Steak"));
        archivio2.setQuantita(Collections.singletonList(1));
        archivio2.setTotale(new BigDecimal("40.00"));
        archivio2.setDataArchiviazione(monday);
        inMemoryDao.store(archivio2);

        Archivio archivio3 = new Archivio();
        archivio3.setIdOrdine(3);
        archivio3.setProdotti(Collections.singletonList("Fish"));
        archivio3.setQuantita(Collections.singletonList(3));
        archivio3.setTotale(new BigDecimal("90.00"));
        archivio3.setDataArchiviazione(tuesday);
        inMemoryDao.store(archivio3);

        // Richiede i guadagni per giorno per un periodo sufficientemente ampio (es. "Trimestre")
        Map<String, Number> guadagniGiorno = controller.guadagniPerGiorno("trimestre");

        // Il mapping prevede:
        // "Lun" (lunedì): 80.00 + 40.00 = 120.00
        // "Mar" (martedì): 90.00
        // Le altre giornate dovrebbero avere valore 0
        assertEquals(120.00, guadagniGiorno.get("Lun").doubleValue());
        assertEquals(90.00, guadagniGiorno.get("Mar").doubleValue());
        assertEquals(0.0, guadagniGiorno.get("Mer").doubleValue());
        assertEquals(0.0, guadagniGiorno.get("Gio").doubleValue());
        assertEquals(0.0, guadagniGiorno.get("Ven").doubleValue());
        assertEquals(0.0, guadagniGiorno.get("Sab").doubleValue());
        assertEquals(0.0, guadagniGiorno.get("Dom").doubleValue());
    }

    @Test
    void testGuadagniPerGiornoInvalid() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                controller.guadagniPerGiorno("annuale")
        );
        assertEquals("Periodo non valido. Deve essere 'settimana', 'mese' o 'trimestre'.", ex.getMessage());
    }
}
