package com.biteme.app.cli;

import java.util.Map;
import com.biteme.app.controller.ArchivioController;

public class AdminHomeCLI {
    private static ArchivioController archivioController = new ArchivioController();
    private static boolean mostraGuadagni = false;

    public static void start() {
        var scanner = CLIUtils.getScanner();
        while(true) {
            System.out.println("========== Admin Home CLI ==========");
            System.out.println("1. Visualizza statistiche");
            System.out.println("2. Switch view (attualmente mostra " + (mostraGuadagni ? "Guadagni" : "Prodotti Ordinati") + ")");
            System.out.println("3. Torna al Menu");
            System.out.print("Scegli un'opzione: ");
            String scelta = scanner.nextLine();

            if(scelta.equals("1")) {
                System.out.print("Inserisci il periodo (es. settimana, mese, anno): ");
                String periodo = scanner.nextLine();
                Map<String, Number> statistiche;
                if(mostraGuadagni) {
                    statistiche = archivioController.guadagniPerGiorno(periodo.toLowerCase());
                } else {
                    statistiche = archivioController.piattiPiuOrdinatiPerPeriodo(periodo.toLowerCase());
                }
                System.out.println("Statistiche:");
                for(Map.Entry<String, Number> entry : statistiche.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
            } else if(scelta.equals("2")) {
                mostraGuadagni = !mostraGuadagni;
                System.out.println("View switchata. Ora viene mostrato: " + (mostraGuadagni ? "Guadagni" : "Prodotti Ordinati"));
            } else if(scelta.equals("3")) {
                break;
            } else {
                System.out.println("Opzione non valida.");
            }
        }
    }
}
