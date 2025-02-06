package com.biteme.app.cli;

import java.util.Map;
import java.util.Scanner;

import com.biteme.app.controller.ArchivioController;

public class AdminHomeCLI {
    private static final ArchivioController ARCHIVIO_CONTROLLER = new ArchivioController();
    private static boolean mostraGuadagni = false;
    private static final String[] MENU_OPTIONS = {
            "1. Visualizza statistiche",
            "2. Switch view (attualmente mostra %s)",
            "3. Torna al Menu"
    };
    private static final String STATS_HEADER = "Statistiche:";
    private static final String VIEW_SWITCH_FORMAT = "View switchata. Ora viene mostrato: %s";

    private AdminHomeCLI() {
        // Private constructor to prevent instantiation
    }

    public static void start() {
        var scanner = CLIUtils.getScanner();
        while(true) {
            printMenu();
            String scelta = scanner.nextLine();

            switch(scelta) {
                case "1" -> handleStatistics(scanner);
                case "2" -> toggleView();
                case "3" -> { return; }
                default -> System.out.println("Opzione non valida.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("========== Admin Home CLI ==========");
        System.out.println(MENU_OPTIONS[0]);
        System.out.printf("%s%n", String.format(MENU_OPTIONS[1], getCurrentView()));
        System.out.println(MENU_OPTIONS[2]);
        System.out.print("Scegli un'opzione: ");
    }

    private static void handleStatistics(Scanner scanner) {
        System.out.print("Inserisci il periodo (es. settimana, mese, anno): ");
        String periodo = scanner.nextLine().toLowerCase();

        Map<String, Number> statistiche = mostraGuadagni
                ? ARCHIVIO_CONTROLLER.guadagniPerGiorno(periodo)
                : ARCHIVIO_CONTROLLER.piattiPiuOrdinatiPerPeriodo(periodo);

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

    private static String getCurrentView() {
        return mostraGuadagni ? "Guadagni" : "Prodotti Ordinati";
    }
}
