package com.biteme.app.controller;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.entities.Archivio;
import com.biteme.app.entities.ArchivioRiga;
import com.biteme.app.entities.Categoria;
import com.biteme.app.entities.Prodotto;
import com.biteme.app.persistence.ArchivioDao;
import com.biteme.app.persistence.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ArchivioController {

    private final ArchivioDao dao;

    private static final String PERIODO_SETTIMANA  = "settimana";
    private static final String PERIODO_MESE       = "mese";
    private static final String PERIODO_TRIMESTRE  = "trimestre";
    private static final String ERRORE_PERIODO     =
            "Periodo non valido. Deve essere 'settimana', 'mese' o 'trimestre'.";

    public ArchivioController() {
        this.dao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getArchivioDao();
    }

    public void archiviaOrdine(ArchivioBean bean) {

        bean.validate();

        List<ArchivioRiga> righeEntita = new ArrayList<>();
        for (int i = 0; i < bean.getProdotti().size(); i++) {
            ProdottoBean pb = bean.getProdotti().get(i);
            int qty = bean.getQuantita().get(i);

            String catString = pb.getCategoria();
            Categoria categoriaEnum;
            if (catString != null && !catString.isBlank()) {
                try {
                    categoriaEnum = Categoria.valueOf(catString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    categoriaEnum = Categoria.EXTRA;
                }
            } else {
                categoriaEnum = Categoria.EXTRA;
            }

            Prodotto prodottoEntita = new Prodotto(
                    pb.getId() != null ? pb.getId() : 0,
                    pb.getNome(),
                    pb.getPrezzo(),
                    categoriaEnum,
                    Boolean.TRUE.equals(pb.getDisponibile())
            );

            righeEntita.add(new ArchivioRiga(prodottoEntita, qty));
        }

        Archivio entity = new Archivio(
                bean.getIdOrdine(),
                righeEntita,
                bean.getTotale(),
                bean.getDataArchiviazione()
        );

        dao.create(entity);

        bean.setIdOrdine(entity.getIdOrdine());
    }

    public Map<String, Number> piattiPiuOrdinatiPerPeriodo(String periodo) {
        LocalDateTime[] dateRange = getDateRange(periodo);
        List<Archivio> archivi = dao.findByDateRange(dateRange[0], dateRange[1]);

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
                        (u,v) -> u,
                        LinkedHashMap::new
                ));
    }

    public Map<String, Number> guadagniPerPeriodo(String periodo) {
        LocalDateTime[] dateRange = getDateRange(periodo);
        List<Archivio> archivi = dao.findByDateRange(dateRange[0], dateRange[1]);

        Map<String, BigDecimal> guadagni = new HashMap<>();
        for (Archivio a : archivi) {
            for (ArchivioRiga r : a.getRighe()) {
                String nome = r.getProdotto().getNome();
                BigDecimal incasso = r.getProdotto().getPrezzo()
                        .multiply(BigDecimal.valueOf(r.getQuantita()));
                guadagni.merge(nome, incasso, BigDecimal::add);
            }
        }

        return guadagni.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Number)e.getValue().doubleValue(),
                        (u,v) -> u,
                        LinkedHashMap::new
                ));
    }

    public Map<String, Number> guadagniPerGiorno(String periodo) {
        LocalDateTime[] dateRange = getDateRange(periodo);
        List<Archivio> archivi = dao.findByDateRange(dateRange[0], dateRange[1]);

        // prepariamo la mappa con tutti i giorni a zero
        List<String> giorni = List.of("Lun","Mar","Mer","Gio","Ven","Sab","Dom");
        Map<String, BigDecimal> mappa = new LinkedHashMap<>();
        giorni.forEach(g -> mappa.put(g, BigDecimal.ZERO));

        // per ogni archivio sommo gli incassi delle righe
        for (Archivio a : archivi) {
            // calcolo l’incasso totale di questo archivio, sulla base delle righe
            BigDecimal totaleGiorno = BigDecimal.ZERO;
            for (ArchivioRiga r : a.getRighe()) {
                BigDecimal incassoRiga = r.getProdotto()
                        .getPrezzo()
                        .multiply(BigDecimal.valueOf(r.getQuantita()));
                totaleGiorno = totaleGiorno.add(incassoRiga);
            }

            String chiave = switch (a.getDataArchiviazione().getDayOfWeek()) {
                case MONDAY    -> "Lun";
                case TUESDAY   -> "Mar";
                case WEDNESDAY -> "Mer";
                case THURSDAY  -> "Gio";
                case FRIDAY    -> "Ven";
                case SATURDAY  -> "Sab";
                case SUNDAY    -> "Dom";
            };
            mappa.put(chiave, mappa.get(chiave).add(totaleGiorno));
        }

        // trasformo in Map<String, Number> con valori double
        return mappa.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Number)e.getValue().doubleValue(),
                        (u,v) -> u,
                        LinkedHashMap::new
                ));
    }

    private LocalDateTime[] getDateRange(String periodo) {
        if (periodo == null || periodo.isBlank()) {
            throw new IllegalArgumentException(ERRORE_PERIODO);
        }
        LocalDateTime end   = LocalDateTime.now();
        LocalDateTime start = switch (periodo.toLowerCase()) {
            case PERIODO_SETTIMANA -> end.minusWeeks(1);
            case PERIODO_MESE      -> end.minusMonths(1);
            case PERIODO_TRIMESTRE -> end.minusMonths(3);
            default -> throw new IllegalArgumentException(ERRORE_PERIODO);
        };
        return new LocalDateTime[]{ start, end };
    }
}