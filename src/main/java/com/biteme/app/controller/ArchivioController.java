package com.biteme.app.controller;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.entities.Archivio;
import com.biteme.app.persistence.ArchivioDao;
import com.biteme.app.persistence.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ArchivioController {
    private ArchivioDao archivioDao;

    private static final String PERIODO_SETTIMANA = "settimana";
    private static final String PERIODO_MESE = "mese";
    private static final String PERIODO_TRIMESTRE = "trimestre";
    private static final String ERRORE_PERIODO_NON_VALIDO =
            "Periodo non valido. Deve essere 'settimana', 'mese' o 'trimestre'.";

    public ArchivioController() {
        this.archivioDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getArchivioDao();
    }

    public void salvaInArchivio(Archivio archivio) {
        archivioDao.store(archivio);
    }

    public void archiviaOrdine(ArchivioBean archivioBean) {
        Archivio archivio = new Archivio();
        archivio.setIdOrdine(archivioBean.getIdOrdine());
        archivio.setProdotti(archivioBean.getProdotti());
        archivio.setQuantita(archivioBean.getQuantita());
        archivio.setTotale(archivioBean.getTotale());
        archivio.setDataArchiviazione(archivioBean.getDataArchiviazione());

        salvaInArchivio(archivio);
    }

    public Map<String, Number> piattiPiuOrdinati(LocalDateTime startDate, LocalDateTime endDate) {
        List<Archivio> archivi = archivioDao.findByDateRange(startDate, endDate);

        Map<String, Integer> conteggioPiatti = new HashMap<>();
        for (Archivio archivio : archivi) {
            for (int i = 0; i < archivio.getProdotti().size(); i++) {
                String prodotto = archivio.getProdotti().get(i);
                int quantita = archivio.getQuantita().get(i);
                conteggioPiatti.merge(prodotto, quantita, Integer::sum);
            }
        }

        return conteggioPiatti.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))                 .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Number) e.getValue(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


    public Map<String, Number> piattiPiuOrdinatiPerPeriodo(String periodo) {
        LocalDateTime[] dateRange = getDateRange(periodo);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        return piattiPiuOrdinati(startDate, endDate);
    }



    private LocalDateTime[] getDateRange(String periodo) {
        if (periodo == null || periodo.trim().isEmpty()) {
            throw new IllegalArgumentException(ERRORE_PERIODO_NON_VALIDO);
        }
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;

        switch (periodo.toLowerCase()) {
            case PERIODO_SETTIMANA:
                startDate = endDate.minusWeeks(1);
                break;
            case PERIODO_MESE:
                startDate = endDate.minusMonths(1);
                break;
            case PERIODO_TRIMESTRE:
                startDate = endDate.minusMonths(3);
                break;
            default:
                throw new IllegalArgumentException(ERRORE_PERIODO_NON_VALIDO);
        }

        return new LocalDateTime[]{ startDate, endDate };
    }



    public Map<String, Number> guadagniPerPeriodo(String periodo) {
        LocalDateTime[] dateRange = getDateRange(periodo);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        List<Archivio> archivi = archivioDao.findByDateRange(startDate, endDate);

        Map<String, BigDecimal> guadagni = new HashMap<>();
        for (Archivio archivio : archivi) {
                        for (int i = 0; i < archivio.getProdotti().size(); i++) {
                String prodotto = archivio.getProdotti().get(i);
                BigDecimal totale = archivio.getTotale();

                guadagni.merge(prodotto, totale, BigDecimal::add);
            }
        }

                return guadagni.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().doubleValue(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


    public Map<String, Number> guadagniPerGiorno(String periodo) {
                LocalDateTime[] dateRange = getDateRange(periodo);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

                List<Archivio> archivi = archivioDao.findByDateRange(startDate, endDate);

                Map<String, BigDecimal> guadagniGiorno = new LinkedHashMap<>();
        String[] giorni = { "Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom" };
        for (String giorno : giorni) {
            guadagniGiorno.put(giorno, BigDecimal.ZERO);
        }

                for (Archivio archivio : archivi) {
            String giornoAbbreviato = switch (archivio.getDataArchiviazione().getDayOfWeek()) {
                case MONDAY -> "Lun";
                case TUESDAY -> "Mar";
                case WEDNESDAY -> "Mer";
                case THURSDAY -> "Gio";
                case FRIDAY -> "Ven";
                case SATURDAY -> "Sab";
                case SUNDAY -> "Dom";
            };
            BigDecimal totaleGiorno = guadagniGiorno.get(giornoAbbreviato);
            guadagniGiorno.put(giornoAbbreviato, totaleGiorno.add(archivio.getTotale()));
        }

                Map<String, Number> result = new LinkedHashMap<>();
        guadagniGiorno.forEach((g, tot) -> result.put(g, tot.doubleValue()));

        return result;
    }

}