package com.biteme.app.controller;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.bean.ArchivioRigaBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entities.Archivio;
import com.biteme.app.persistence.ArchivioDao;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.persistence.inmemory.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

//@author Kevin Hoxha

class ArchivioControllerTest {

    private ArchivioController controller;
    private ArchivioDao archivioDao;
    private final List<Integer> createdArchivioIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        controller = new ArchivioController();
        archivioDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getArchivioDao();
        Field daoField = ArchivioController.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(controller, archivioDao);
    }

    @AfterEach
    void tearDown() {
        if (Configuration.getPersistenceProvider().getDaoFactory()
                .getClass().getName().contains("InMemoryDaoFactory")) {
            Storage.getInstance().getArchivi().clear();
        } else {
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
        int newId = 1000;

        ArchivioBean bean = new ArchivioBean();
        bean.setIdOrdine(newId);
        bean.setTotale(new BigDecimal("30.50"));
        bean.setDataArchiviazione(LocalDateTime.now());

        ProdottoBean p1 = new ProdottoBean();
        p1.setId(1);
        p1.setNome("Pizza");
        p1.setPrezzo(new BigDecimal("8.50"));
        p1.setCategoria("Pizze");
        p1.setDisponibile(true);
        ArchivioRigaBean rb1 = new ArchivioRigaBean();
        rb1.setProdottoBean(p1);
        rb1.setQuantita(2);

        ProdottoBean p2 = new ProdottoBean();
        p2.setId(2);
        p2.setNome("Pasta");
        p2.setPrezzo(new BigDecimal("6.75"));
        p2.setCategoria("Primi");
        p2.setDisponibile(true);
        ArchivioRigaBean rb2 = new ArchivioRigaBean();
        rb2.setProdottoBean(p2);
        rb2.setQuantita(3);

        bean.setRighe(Arrays.asList(rb1, rb2));
        controller.archiviaOrdine(bean);

        Archivio saved = archivioDao.getAll().stream()
                .filter(a -> a.getIdOrdine() == newId)
                .findFirst()
                .orElseThrow();
        createdArchivioIds.add(saved.getIdOrdine());

        assertEquals(new BigDecimal("30.50"), saved.getTotale());
        assertNotNull(saved.getDataArchiviazione());
        Map<String,Integer> mappa = saved.getRighe().stream()
                .collect(Collectors.toMap(
                        r -> r.getProdotto().getNome(),
                        r -> r.getQuantita()
                ));
        assertEquals(2, mappa.get("Pizza").intValue());
        assertEquals(3, mappa.get("Pasta").intValue());
    }

    @Test
    void testPiattiPiuOrdinati() {
        LocalDateTime now = LocalDateTime.now();

        ArchivioBean b1 = new ArchivioBean();
        b1.setIdOrdine(2000);
        b1.setDataArchiviazione(now.minusDays(1));
        b1.setTotale(new BigDecimal("25.00"));

        ProdottoBean p1 = new ProdottoBean();
        p1.setId(1);
        p1.setNome("Pizza");
        p1.setPrezzo(new BigDecimal("8.00"));
        p1.setCategoria("Pizze");
        p1.setDisponibile(true);
        ArchivioRigaBean r1 = new ArchivioRigaBean();
        r1.setProdottoBean(p1);
        r1.setQuantita(2);

        ProdottoBean p2 = new ProdottoBean();
        p2.setId(2);
        p2.setNome("Pasta");
        p2.setPrezzo(new BigDecimal("7.00"));
        p2.setCategoria("Primi");
        p2.setDisponibile(true);
        ArchivioRigaBean r2 = new ArchivioRigaBean();
        r2.setProdottoBean(p2);
        r2.setQuantita(3);

        b1.setRighe(Arrays.asList(r1, r2));
        controller.archiviaOrdine(b1);

        ArchivioBean b2 = new ArchivioBean();
        b2.setIdOrdine(2001);
        b2.setDataArchiviazione(now.minusDays(2));
        b2.setTotale(new BigDecimal("30.00"));

        ProdottoBean p3 = new ProdottoBean();
        p3.setId(1);
        p3.setNome("Pizza");
        p3.setPrezzo(new BigDecimal("8.00"));
        p3.setCategoria("Pizze");
        p3.setDisponibile(true);
        ArchivioRigaBean r3 = new ArchivioRigaBean();
        r3.setProdottoBean(p3);
        r3.setQuantita(1);

        ProdottoBean p4 = new ProdottoBean();
        p4.setId(3);
        p4.setNome("Salad");
        p4.setPrezzo(new BigDecimal("5.00"));
        p4.setCategoria("Contorni");
        p4.setDisponibile(true);
        ArchivioRigaBean r4 = new ArchivioRigaBean();
        r4.setProdottoBean(p4);
        r4.setQuantita(4);

        b2.setRighe(Arrays.asList(r3, r4));
        controller.archiviaOrdine(b2);

        Map<String, Number> result = controller.piattiPiuOrdinatiPerPeriodo("trimestre");
        assertEquals(4, result.get("Salad").intValue());
        assertEquals(3, result.get("Pizza").intValue());
        assertEquals(3, result.get("Pasta").intValue());
    }

    @Test
    void testPiattiPiuOrdinatiPerPeriodoValid() {
        LocalDateTime now = LocalDateTime.now();

        ArchivioBean b = new ArchivioBean();
        b.setIdOrdine(3000);
        b.setDataArchiviazione(now.minusDays(2));
        b.setTotale(new BigDecimal("50.00"));

        ProdottoBean p = new ProdottoBean();
        p.setId(1);
        p.setNome("Burger");
        p.setPrezzo(new BigDecimal("10.00"));
        p.setCategoria("Secondi");
        p.setDisponibile(true);
        ArchivioRigaBean r = new ArchivioRigaBean();
        r.setProdottoBean(p);
        r.setQuantita(5);

        b.setRighe(Collections.singletonList(r));
        controller.archiviaOrdine(b);

        assertEquals(5, controller.piattiPiuOrdinatiPerPeriodo("settimana").get("Burger").intValue());
        assertEquals(5, controller.piattiPiuOrdinatiPerPeriodo("mese").get("Burger").intValue());
        assertEquals(5, controller.piattiPiuOrdinatiPerPeriodo("trimestre").get("Burger").intValue());
    }

    @Test
    void testPiattiPiuOrdinatiPerPeriodoInvalid() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                controller.piattiPiuOrdinatiPerPeriodo("annuale"));
        assertEquals("Periodo non valido. Deve essere 'settimana', 'mese' o 'trimestre'.", ex.getMessage());
    }

    @Test
    void testGuadagniPerPeriodo() {
        LocalDateTime now = LocalDateTime.now();

        ArchivioBean b = new ArchivioBean();
        b.setIdOrdine(4000);
        b.setDataArchiviazione(now.minusDays(2));
        b.setTotale(new BigDecimal("100.00"));

        ProdottoBean p = new ProdottoBean();
        p.setId(1);
        p.setNome("Sushi");
        p.setPrezzo(new BigDecimal("20.00"));
        p.setCategoria("Secondi");
        p.setDisponibile(true);
        ArchivioRigaBean r = new ArchivioRigaBean();
        r.setProdottoBean(p);
        r.setQuantita(5);

        b.setRighe(Collections.singletonList(r));
        controller.archiviaOrdine(b);

        Number guadagno = controller.guadagniPerPeriodo("settimana").get("Sushi");
        assertNotNull(guadagno);
        assertEquals(100.0, guadagno.doubleValue(), 0.001);
    }

    @Test
    void testGuadagniPerGiorno() {
        LocalDateTime monday = LocalDateTime.of(2025, 5, 5, 12, 0);
        LocalDateTime tuesday = LocalDateTime.of(2025, 5, 6, 12, 0);

        ArchivioBean b1 = new ArchivioBean();
        b1.setIdOrdine(5001);
        b1.setDataArchiviazione(monday);
        b1.setTotale(new BigDecimal("80.00"));

        ProdottoBean p1 = new ProdottoBean();
        p1.setId(1);
        p1.setNome("Steak");
        p1.setPrezzo(new BigDecimal("40.00"));
        p1.setCategoria("Secondi");
        p1.setDisponibile(true);
        ArchivioRigaBean r1 = new ArchivioRigaBean();
        r1.setProdottoBean(p1);
        r1.setQuantita(2);

        b1.setRighe(Collections.singletonList(r1));
        controller.archiviaOrdine(b1);

        ArchivioBean b2 = new ArchivioBean();
        b2.setIdOrdine(5002);
        b2.setDataArchiviazione(monday);
        b2.setTotale(new BigDecimal("40.00"));

        ProdottoBean p2 = new ProdottoBean();
        p2.setId(1);
        p2.setNome("Steak");
        p2.setPrezzo(new BigDecimal("40.00"));
        p2.setCategoria("Secondi");
        p2.setDisponibile(true);
        ArchivioRigaBean r2 = new ArchivioRigaBean();
        r2.setProdottoBean(p2);
        r2.setQuantita(1);

        b2.setRighe(Collections.singletonList(r2));
        controller.archiviaOrdine(b2);

        ArchivioBean b3 = new ArchivioBean();
        b3.setIdOrdine(6000);
        b3.setDataArchiviazione(tuesday);
        b3.setTotale(new BigDecimal("90.00"));

        ProdottoBean p3 = new ProdottoBean();
        p3.setId(2);
        p3.setNome("Fish");
        p3.setPrezzo(new BigDecimal("30.00"));
        p3.setCategoria("Secondi");
        p3.setDisponibile(true);
        ArchivioRigaBean r3 = new ArchivioRigaBean();
        r3.setProdottoBean(p3);
        r3.setQuantita(3);

        b3.setRighe(Collections.singletonList(r3));
        controller.archiviaOrdine(b3);

        Map<String, Number> perGiorno = controller.guadagniPerGiorno("trimestre");
        assertEquals(120.0, perGiorno.get("Lun").doubleValue(), 0.001);
        assertEquals(90.0, perGiorno.get("Mar").doubleValue(), 0.001);
        for (String g : List.of("Mer", "Gio", "Ven", "Sab", "Dom")) {
            assertEquals(0.0, perGiorno.get(g).doubleValue(), 0.001);
        }
    }

    @Test
    void testGuadagniPerGiornoInvalid() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                controller.guadagniPerGiorno("annuale"));
        assertEquals("Periodo non valido. Deve essere 'settimana', 'mese' o 'trimestre'.", ex.getMessage());
    }
}