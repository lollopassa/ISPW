package com.biteme.app.controller;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.persistence.ArchivioDao;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.persistence.inmemory.Storage;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

//@author Kevin Hoxha

class ArchivioControllerTest {

    private ArchivioController controller;
    private ArchivioDao archivioDao;
    private final List<Integer> createdIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        controller  = new ArchivioController();
        archivioDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getArchivioDao();
        // svuota l’archivio in-memory, se usato
        if (Configuration.getPersistenceProvider().getDaoFactory()
                .getClass().getName().contains("InMemoryDaoFactory")) {
            Storage.getInstance().getArchivi().clear();
        }
    }

    @AfterEach
    void tearDown() {
        // se persistente, rimuovi gli inserimenti
        createdIds.forEach(id -> {
            if (archivioDao.exists(id)) archivioDao.delete(id);
        });
        createdIds.clear();
    }

    @Test
    void piattiPiuOrdinatiPerPeriodo_funziona() {
        LocalDateTime now = LocalDateTime.now();
        // primo archivio: 2×A
        ArchivioBean b1 = new ArchivioBean();
        b1.setIdOrdine(200);
        b1.setDataArchiviazione(now.minusDays(1));
        b1.setTotale(BigDecimal.ZERO);
        ProdottoBean p1 = new ProdottoBean(); p1.setId(1); p1.setNome("A"); p1.setCategoria("Extra"); p1.setPrezzo(BigDecimal.ONE); p1.setDisponibile(true);
        b1.setProdotti(Collections.singletonList(p1));
        b1.setQuantita(Collections.singletonList(2));
        controller.archiviaOrdine(b1);

        // secondo: 3×B
        ArchivioBean b2 = new ArchivioBean();
        b2.setIdOrdine(201);
        b2.setDataArchiviazione(now.minusDays(2));
        b2.setTotale(BigDecimal.ZERO);
        ProdottoBean p2 = new ProdottoBean(); p2.setId(2); p2.setNome("B"); p2.setCategoria("Extra"); p2.setPrezzo(BigDecimal.ONE); p2.setDisponibile(true);
        b2.setProdotti(Collections.singletonList(p2));
        b2.setQuantita(Collections.singletonList(3));
        controller.archiviaOrdine(b2);

        var result = controller.piattiPiuOrdinatiPerPeriodo("trimestre");
        assertFalse(result.isEmpty(), "La mappa dei piatti ordinati non può essere vuota");
        assertEquals(3, result.get("B").intValue());
        assertEquals(2, result.get("A").intValue());
    }

    @Test
    void guadagniPerPeriodo_calcolaCorrettamente() {
        LocalDateTime now = LocalDateTime.now();
        ArchivioBean b = new ArchivioBean();
        b.setIdOrdine(300);
        b.setDataArchiviazione(now.minusDays(1));
        b.setTotale(BigDecimal.ZERO);
        ProdottoBean p = new ProdottoBean(); p.setId(1); p.setNome("X"); p.setCategoria("Extra"); p.setPrezzo(new BigDecimal("5")); p.setDisponibile(true);
        b.setProdotti(Collections.singletonList(p));
        b.setQuantita(Collections.singletonList(4)); // 4×5 = 20
        controller.archiviaOrdine(b);

        var guadagni = controller.guadagniPerPeriodo("mese");
        assertTrue(guadagni.containsKey("X"), "Mappa guadagni non contiene la chiave 'X'");
        assertEquals(20.0, guadagni.get("X").doubleValue(), 0.001);
    }

    @Test
    void guadagniPerGiorno_funziona() {
        LocalDateTime monday = LocalDateTime.now()
                .with(java.time.DayOfWeek.MONDAY)
                .withHour(10).withMinute(0);
        ArchivioBean b = new ArchivioBean();
        b.setIdOrdine(400);
        b.setDataArchiviazione(monday);
        b.setTotale(BigDecimal.ZERO);
        ProdottoBean p = new ProdottoBean(); p.setId(1); p.setNome("Y"); p.setCategoria("Extra"); p.setPrezzo(new BigDecimal("10")); p.setDisponibile(true);
        b.setProdotti(Collections.singletonList(p));
        b.setQuantita(Collections.singletonList(2)); // 2×10 = 20
        controller.archiviaOrdine(b);

        var perGiorno = controller.guadagniPerGiorno("trimestre");
        assertEquals(20.0, perGiorno.get("Lun").doubleValue(), 0.001);
    }

    @Test
    void periodiInvalidi_lancianoEccezione() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.piattiPiuOrdinatiPerPeriodo("anno"));
        assertThrows(IllegalArgumentException.class,
                () -> controller.guadagniPerPeriodo("anno"));
        assertThrows(IllegalArgumentException.class,
                () -> controller.guadagniPerGiorno("anno"));
    }
}
