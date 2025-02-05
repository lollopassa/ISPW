package com.biteme.app.controller;

import com.biteme.app.bean.ArchivioBean;
import com.biteme.app.entity.Archivio;
import com.biteme.app.persistence.ArchivioDao;
import com.biteme.app.util.Configuration;
import java.util.LinkedHashMap;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArchivioController {
    private ArchivioDao archivioDao;

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

        // Convertiamo a Map<String, Number> e utilizziamo LinkedHashMap per mantenere l'ordine
        return conteggioPiatti.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Ordine decrescente
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Number) e.getValue(), // Conversione a Number
                        (e1, e2) -> e1, // Gestione conflitti, anche se non necessaria
                        LinkedHashMap::new // Ordina preservando l'ordine
                ));
    }

    public Map<String, Number> piattiPiuOrdinatiPerPeriodo(String periodo) {
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        switch (periodo.toLowerCase()) {
            case "settimana":
                startDate = endDate.minusWeeks(1);
                break;
            case "mese":
                startDate = endDate.minusMonths(1);
                break;
            case "trimestre":
                startDate = endDate.minusMonths(3);
                break;
            default:
                throw new IllegalArgumentException("Periodo non valido. Usa 'settimana', 'mese', o 'trimestre'.");
        }

        return piattiPiuOrdinati(startDate, endDate);
    }

    public Map<String, Number> guadagniPerPeriodo(String periodo) {
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        switch (periodo.toLowerCase()) {
            case "settimana":
                startDate = endDate.minusWeeks(1);
                break;
            case "mese":
                startDate = endDate.minusMonths(1);
                break;
            case "trimestre":
                startDate = endDate.minusMonths(3);
                break;
            default:
                throw new IllegalArgumentException("Periodo non valido. Usa 'settimana', 'mese', o 'trimestre'.");
        }

        List<Archivio> archivi = archivioDao.findByDateRange(startDate, endDate);

        Map<String, BigDecimal> guadagni = new HashMap<>();
        for (Archivio archivio : archivi) {
            for (int i = 0; i < archivio.getProdotti().size(); i++) {
                String prodotto = archivio.getProdotti().get(i);
                BigDecimal totale = archivio.getTotale();

                guadagni.merge(prodotto, totale, BigDecimal::add);
            }
        }

        // Convertiamo a Map<String, Number> per conformità con il contratto del metodo
        return guadagni.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().doubleValue(), // Conversione a Double (Number)
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}