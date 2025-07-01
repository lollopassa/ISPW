package com.biteme.app.controller;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.entities.Archivio;
import com.biteme.app.entities.ArchivioRiga;
import com.biteme.app.persistence.ArchivioDao;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.util.mapper.BeanEntityMapperFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ArchivioController {

    private final ArchivioDao dao;
    private final BeanEntityMapperFactory mapper;

    private static final String PERIODO_SETTIMANA  = "settimana";
    private static final String PERIODO_MESE       = "mese";
    private static final String PERIODO_TRIMESTRE  = "trimestre";
    private static final String ERRORE_PERIODO     =
            "Periodo non valido. Deve essere 'settimana', 'mese' o 'trimestre'.";

    public ArchivioController() {
        this.dao    = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getArchivioDao();
        this.mapper = BeanEntityMapperFactory.getInstance();
    }

    public void salvaInArchivio(Archivio archivio) {
        dao.create(archivio);
    }

    public void archiviaOrdine(ArchivioBean bean) {
        Archivio entity = mapper.toEntity(bean, ArchivioBean.class);
        salvaInArchivio(entity);
        bean.setIdOrdine(entity.getIdOrdine());
    }

    public Map<String, Number> piattiPiuOrdinatiPerPeriodo(String periodo) {
        LocalDateTime[] range = getDateRange(periodo);
        return piattiPiuOrdinati(range[0], range[1]);
    }

    private Map<String, Number> piattiPiuOrdinati(LocalDateTime start, LocalDateTime end) {
        List<Archivio> archivi = dao.findByDateRange(start, end);
        Map<String, Integer> conteggi = new HashMap<>();

        for (Archivio a : archivi) {
            for (ArchivioRiga r : a.getRighe()) {
                String nome = r.getProdotto().getNome();
                conteggi.merge(nome, r.getQuantita(), Integer::sum);
            }
        }

        return conteggi.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Number)e.getValue(),
                        (u,v)->u,
                        LinkedHashMap::new
                ));
    }

    public Map<String, Number> guadagniPerPeriodo(String periodo) {
        LocalDateTime[] range = getDateRange(periodo);
        List<Archivio> archivi = dao.findByDateRange(range[0], range[1]);

        Map<String, BigDecimal> guadagni = new HashMap<>();
        for (Archivio a : archivi) {
            for (ArchivioRiga r : a.getRighe()) {
                String nome = r.getProdotto().getNome();
                BigDecimal prezzo = r.getProdotto().getPrezzo()
                        .multiply(BigDecimal.valueOf(r.getQuantita()));
                guadagni.merge(nome, prezzo, BigDecimal::add);
            }
        }

        return guadagni.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Number)e.getValue().doubleValue(),
                        (u,v)->u,
                        LinkedHashMap::new
                ));
    }

    public Map<String, Number> guadagniPerGiorno(String periodo) {
        LocalDateTime[] range = getDateRange(periodo);
        List<Archivio> archivi = dao.findByDateRange(range[0], range[1]);

        List<String> giorni = List.of("Lun","Mar","Mer","Gio","Ven","Sab","Dom");
        Map<String, BigDecimal> mappa = new LinkedHashMap<>();
        giorni.forEach(g -> mappa.put(g, BigDecimal.ZERO));

        for (Archivio a : archivi) {
            String g = switch (a.getDataArchiviazione().getDayOfWeek()) {
                case MONDAY    -> "Lun";
                case TUESDAY   -> "Mar";
                case WEDNESDAY -> "Mer";
                case THURSDAY  -> "Gio";
                case FRIDAY    -> "Ven";
                case SATURDAY  -> "Sab";
                case SUNDAY    -> "Dom";
            };
            mappa.put(g, mappa.get(g).add(a.getTotale()));
        }

        return mappa.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Number)e.getValue().doubleValue(),
                        (u,v)->u,
                        LinkedHashMap::new
                ));
    }

    private LocalDateTime[] getDateRange(String periodo) {
        if (periodo == null || periodo.isBlank())
            throw new IllegalArgumentException(ERRORE_PERIODO);

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = switch(periodo.toLowerCase()) {
            case PERIODO_SETTIMANA -> end.minusWeeks(1);
            case PERIODO_MESE      -> end.minusMonths(1);
            case PERIODO_TRIMESTRE -> end.minusMonths(3);
            default -> throw new IllegalArgumentException(ERRORE_PERIODO);
        };
        return new LocalDateTime[]{start, end};
    }
}
