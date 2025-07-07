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

    @BeforeEach
    void setUp() {
        controller = new ArchivioController();
        archivioDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getArchivioDao();
        // se stiamo usando l’implementazione in memoria, svuoto lo storage
        if (Configuration.getPersistenceProvider().getDaoFactory()
                .getClass().getName().contains("InMemoryDaoFactory")) {
            Storage.getInstance().getArchivi().clear();
        } else {
            // altrimenti elimino eventuali residui
            archivioDao.getAll().forEach(a -> archivioDao.delete(a.getIdOrdine()));
        }
    }

    @AfterEach
    void tearDown() {
        // pulisco eventuali archivi rimasti
        archivioDao.getAll().forEach(a -> archivioDao.delete(a.getIdOrdine()));
    }



    @Test
    void piattiPiuOrdinatiPerPeriodo_funziona() {
        LocalDateTime now = LocalDateTime.now();

        // primo archivio, 2 x A
        ArchivioBean b1 = new ArchivioBean();
        b1.setIdOrdine(201);
        b1.setDataArchiviazione(now.minusDays(1));
        b1.setTotale(BigDecimal.ZERO);
        ProdottoBean pa = new ProdottoBean();
        pa.setId(1); pa.setNome("A"); pa.setCategoria("Extra"); pa.setPrezzo(BigDecimal.ONE); pa.setDisponibile(true);
        b1.setProdotti(Collections.singletonList(pa));
        b1.setQuantita(Collections.singletonList(2));
        controller.archiviaOrdine(b1);

        // secondo archivio, 3 x B
        ArchivioBean b2 = new ArchivioBean();
        b2.setIdOrdine(202);
        b2.setDataArchiviazione(now.minusDays(2));
        b2.setTotale(BigDecimal.ZERO);
        ProdottoBean pb = new ProdottoBean();
        pb.setId(2); pb.setNome("B"); pb.setCategoria("Extra"); pb.setPrezzo(BigDecimal.ONE); pb.setDisponibile(true);
        b2.setProdotti(Collections.singletonList(pb));
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

        ArchivioBean bean = new ArchivioBean();
        bean.setIdOrdine(301);
        bean.setDataArchiviazione(now.minusDays(1));
        bean.setTotale(BigDecimal.ZERO);
        ProdottoBean px = new ProdottoBean();
        px.setId(1); px.setNome("X"); px.setCategoria("Extra"); px.setPrezzo(new BigDecimal("5")); px.setDisponibile(true);
        bean.setProdotti(Collections.singletonList(px));
        bean.setQuantita(Collections.singletonList(4));
        controller.archiviaOrdine(bean);

        var guadagni = controller.guadagniPerPeriodo("mese");
        assertTrue(guadagni.containsKey("X"), "Mappa guadagni non contiene la chiave 'X'");
        assertEquals(20.0, guadagni.get("X").doubleValue(), 0.001);
    }

    @Test
    void guadagniPerGiorno_funziona() {
        // imposto la data a lunedì della settimana attuale
        LocalDateTime monday = LocalDateTime.now()
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        ArchivioBean bean = new ArchivioBean();
        bean.setIdOrdine(401);
        bean.setDataArchiviazione(monday);
        bean.setTotale(BigDecimal.ZERO);
        ProdottoBean py = new ProdottoBean();
        py.setId(1); py.setNome("Y"); py.setCategoria("Extra"); py.setPrezzo(new BigDecimal("10")); py.setDisponibile(true);
        bean.setProdotti(Collections.singletonList(py));
        bean.setQuantita(Collections.singletonList(2));
        controller.archiviaOrdine(bean);

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
