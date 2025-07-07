package com.biteme.app.controller;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.entities.StatoOrdinazione;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.persistence.OrdinazioneDao;
import com.biteme.app.persistence.inmemory.Storage;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@author Lorenzo Passacantilli

class GestioneOrdiniControllerTest {

    private GestioneOrdiniController controller;
    private OrdinazioneDao ordinazioneDao;

    @BeforeEach
    void setUp() {
        controller     = new GestioneOrdiniController();
        ordinazioneDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getOrdinazioneDao();
        clearStorage();
    }

    @AfterEach
    void tearDown() {
        clearStorage();
    }

    private void clearStorage() {

        if (Configuration.getPersistenceProvider().getDaoFactory()
                instanceof com.biteme.app.persistence.inmemory.InMemoryDaoFactory) {
            Storage.getInstance().getOrdinazioni().clear();
        }

        ordinazioneDao.getAll().forEach(o -> ordinazioneDao.delete(o.getId()));
    }

    @Test
    void getOrdinazioni_returnsMappedBeans() throws Exception {

        OrdinazioneBean bean = new OrdinazioneBean();
        bean.setNome("Anna Verdi");
        bean.setNumeroClienti("3");
        bean.setTipoOrdine("Asporto");
        bean.setInfoTavolo("Nessuna");
        bean.setOrarioCreazione("19:00");
        controller.creaOrdinazione(bean);

        List<OrdinazioneBean> ordini = controller.getOrdinazioni();

        List<OrdinazioneBean> found = ordini.stream()
                .filter(b -> b.getId() == bean.getId())
                .toList();

        assertEquals(1, found.size(), "L'ordinazione appena creata deve essere presente esattamente una volta");
        OrdinazioneBean b = found.get(0);
        assertEquals(bean.getId(), b.getId());
        assertEquals("Anna Verdi", b.getNome());
        assertEquals("3", b.getNumeroClienti());
        assertEquals("Asporto", b.getTipoOrdine());
        assertEquals("Nessuna", b.getInfoTavolo());
        assertEquals("NUOVO", b.getStatoOrdine());
        assertEquals("19:00", b.getOrarioCreazione());
    }

    @Test
    void eliminaOrdinazione_rimuoveDalDao() throws Exception {

        OrdinazioneBean bean = new OrdinazioneBean();
        bean.setNome("Mario Rossi");
        bean.setNumeroClienti("4");
        bean.setTipoOrdine("Al Tavolo");
        bean.setInfoTavolo("5");
        bean.setOrarioCreazione("20:00");
        controller.creaOrdinazione(bean);
        int idDaEliminare = bean.getId();

        controller.eliminaOrdinazione(idDaEliminare);

        assertFalse(ordinazioneDao.exists(idDaEliminare));
    }

    @Test
    void eliminaOrdinazione_ordIdNonEsistente_lanciaEccezione() {

        OrdinazioneException ex = assertThrows(
                OrdinazioneException.class,
                () -> controller.eliminaOrdinazione(999)
        );
        assertEquals("L'ordinazione con ID 999 non esiste.", ex.getMessage());
    }

    @Test
    void aggiornaStatoOrdinazione_modificaValue() throws Exception {

        OrdinazioneBean bean = new OrdinazioneBean();
        bean.setNome("Giovanna Bianchi");
        bean.setNumeroClienti("2");
        bean.setTipoOrdine("Asporto");
        bean.setInfoTavolo("Nessuna");
        bean.setOrarioCreazione("18:00");
        controller.creaOrdinazione(bean);
        int id = bean.getId();

        controller.aggiornaStatoOrdinazione(id, StatoOrdinazione.IN_CORSO);

        OrdinazioneBean updated = controller.getOrdinazioni().stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElseThrow();
        assertEquals("IN_CORSO", updated.getStatoOrdine());
    }

    @Test
    void aggiornaStatoOrdinazione_ordIdNonEsistente_nonLanciaEccezione() {

        assertDoesNotThrow(() -> controller.aggiornaStatoOrdinazione(999, StatoOrdinazione.IN_CORSO));
        assertFalse(ordinazioneDao.exists(999));
    }
}