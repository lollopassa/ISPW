package com.biteme.app.cli;

import java.util.Map;
import java.util.Scanner;

import com.biteme.app.controller.ArchivioController;

public class AdminHomeCLI {
    private static final ArchivioController ARCHIVIO_CONTROLLER = new ArchivioController();
    private static boolean mostraGuadagni = false;
    private static boolean usaGuadagniAggregati = false;

    private static final String[] MENU_OPTIONS = {
            "1. Visualizza statistiche",
            "2. Switch view (attualmente mostra: %s)",
            "3. Switch aggregazione (attualmente mostra: %s)",
            "4. Torna al Menu"
    };

    private static final String STATS_HEADER = "Statistiche:";
    private static final String VIEW_SWITCH_FORMAT = "View switchata. Ora viene mostrato: %s";
    private static final String AGGREGATION_SWITCH_FORMAT = "Aggregazione switchata. Ora vengono mostrati: %s";

    private static final String GUADAGNI_GIORNALIERO = "Guadagni Giornalieri";
    private static final String GUADAGNI_AGGREGATI = "Guadagni Aggregati";

    // Costruttore privato per evitare istanziazioni
    private AdminHomeCLI() {
    }

    public static void start() {
        Scanner scanner = CLIUtils.getScanner();
        while (true) {
            printMenu();
            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1" -> handleStatistics(scanner);
                case "2" -> toggleView();
                case "3" -> toggleAggregazione();
                case "4" -> { return; }
                default -> System.out.println("Opzione non valida.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("========== Admin Home CLI ==========");
        System.out.println(MENU_OPTIONS[0]);
        System.out.printf("%s%n", String.format(MENU_OPTIONS[1], getCurrentView()));
        System.out.printf("%s%n", String.format(MENU_OPTIONS[2], getCurrentAggregationView()));
        System.out.println(MENU_OPTIONS[3]);
        System.out.print("Scegli un'opzione: ");
    }

    private static void handleStatistics(Scanner scanner) {
        System.out.print("Inserisci il periodo (es. settimana, mese, trimestre): ");
        String periodo = scanner.nextLine().toLowerCase();

        Map<String, Number> statistiche;
        if (mostraGuadagni) {
            // Se in modalità guadagni, decidiamo in base al flag usaGuadagniAggregati
            if (usaGuadagniAggregati) {
                statistiche = ARCHIVIO_CONTROLLER.guadagniPerPeriodo(periodo);
            } else {
                statistiche = ARCHIVIO_CONTROLLER.guadagniPerGiorno(periodo);
            }
        } else {
            statistiche = ARCHIVIO_CONTROLLER.piattiPiuOrdinatiPerPeriodo(periodo);
        }

        displayStatistics(statistiche);
    }

    private static void displayStatistics(Map<String, Number> statistiche) {
        System.out.println(STATS_HEADER);
        statistiche.forEach((key, value) ->
                System.out.printf("%s : %s%n", key, value)
        );
    }

    private static void toggleView() {
        mostraGuadagni = !mostraGuadagni;
        System.out.printf(VIEW_SWITCH_FORMAT + "%n", getCurrentView());
    }

    private static void toggleAggregazione() {
        // Invertiamo il flag per l'aggregazione dei guadagni
        usaGuadagniAggregati = !usaGuadagniAggregati;
        System.out.printf(AGGREGATION_SWITCH_FORMAT + "%n", getCurrentAggregationView());
    }

    private static String getCurrentView() {
        return mostraGuadagni ? "Guadagni" : "Prodotti Ordinati";
    }

    private static String getCurrentAggregationView() {
        return usaGuadagniAggregati ? GUADAGNI_AGGREGATI : GUADAGNI_GIORNALIERO;
    }
}
