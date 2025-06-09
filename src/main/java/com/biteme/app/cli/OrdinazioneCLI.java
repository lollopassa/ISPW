package com.biteme.app.cli;

import java.util.List;
import java.util.Scanner;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.boundary.OrdinazioneBoundary;
import com.biteme.app.exception.ArchiviazioneException;
import com.biteme.app.exception.OrdinazioneException;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.util.CLIUtils;

public class OrdinazioneCLI {
    private static final OrdinazioneBoundary boundary = new OrdinazioneBoundary();
    private static final Scanner SCANNER = CLIUtils.getScanner();

    private static final String INVALID_ID_MESSAGE = "ID non valido.";
    private static final String NO_ORDERS_MESSAGE = "Nessun ordine disponibile.";
    private static final String ORDER_HEADER = "========== Ordinazione CLI ==========";
    private static final String[] MENU_OPTIONS = {
            "1. Crea Ordine",
            "2. Modifica Ordine",
            "3. Elimina Ordine",
            "4. Archivia Ordine",
            "5. Lista Ordini",
            "6. Torna al Menu"
    };

    private OrdinazioneCLI() {
    }

    public static void start() throws OrdineException {
        boolean exit = false;
        while (!exit) {
            printMenu();
            String choice = SCANNER.nextLine();

            switch (choice) {
                case "1" -> handleCreateOrder();
                case "2" -> handleModifyOrder();
                case "3" -> handleDeleteOrder();
                case "4" -> handleArchiveOrder();
                case "5" -> listOrders();
                case "6" -> exit = true;
                default -> System.out.println("Opzione non valida.");
            }
        }
    }

    private static void printMenu() {
        System.out.println(ORDER_HEADER);
        for (String option : MENU_OPTIONS) {
            System.out.println(option);
        }
        System.out.print("Scegli un'opzione: ");
    }

    private static void handleCreateOrder() {
        try {
            System.out.print("Nome Cliente: ");
            String nome = SCANNER.nextLine().trim();

            System.out.print("Tipo Ordine (Al Tavolo/Asporto): ");
            String tipo = SCANNER.nextLine().trim();

            System.out.print("Orario (HH:mm): ");
            String orario = SCANNER.nextLine().trim();

            System.out.print("Coperti: ");
            String coperti = SCANNER.nextLine().trim();

            System.out.print("Info Tavolo (se applicabile): ");
            String tavolo = SCANNER.nextLine().trim();
            boundary.createOrdinazione(nome, tipo, orario, coperti, tavolo);
            System.out.println("Ordine creato con successo per " + nome + ".");
        } catch (OrdinazioneException e) {
            System.err.println("Errore: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Errore imprevisto: " + e.getMessage());
        }
    }
    private static void handleModifyOrder() throws OrdineException {
        List<OrdinazioneBean> orders = boundary.getAll();
        if (showOrderList(orders)) return;

        int id = promptForOrderId();
        if (id != -1) {
            OrdinazioneBean selected = orders.stream()
                    .filter(o -> o.getId() == id)
                    .findFirst()
                    .orElse(null);
            if (selected != null) {
                OrdineCLI.start(id);
            } else {
                System.out.println("Ordine non trovato.");
            }
        }
    }

    private static void handleDeleteOrder() {
        List<OrdinazioneBean> orders = boundary.getAll();
        if (showOrderList(orders)) return;

        int id = promptForOrderId();
        if (id != -1) {
            try {
                boundary.delete(id);
                System.out.println("Ordine eliminato.");
            } catch (OrdinazioneException e) {
                System.err.println("Errore durante l'eliminazione dell'ordine: " + e.getMessage());
            }
        }
    }

    private static void handleArchiveOrder() {
        List<OrdinazioneBean> orders = boundary.getAll();
        if (showOrderList(orders)) return;

        int id = promptForOrderId();
        if (id != -1) {
            OrdinazioneBean selected = orders.stream()
                    .filter(o -> o.getId() == id)
                    .findFirst()
                    .orElse(null);
            if (selected == null) {
                System.out.println("Ordine non trovato.");
                return;
            }
            try {
                boundary.archive(selected);
                System.out.println("Ordine archiviato con successo.");
            } catch (ArchiviazioneException e) {
                System.out.println("Errore durante l'archiviazione: " + e.getMessage());
            }
        }
    }

    private static boolean showOrderList(List<OrdinazioneBean> orders) {
        if (orders.isEmpty()) {
            System.out.println(NO_ORDERS_MESSAGE);
            return true;
        }
        System.out.println("Lista Ordini:");
        orders.forEach(o -> System.out.println(o.getId() + " - " + o.getNome()));
        return false;
    }

    private static int promptForOrderId() {
        System.out.print("Inserisci l'ID dell'ordine: ");
        try {
            return Integer.parseInt(SCANNER.nextLine());
        } catch (NumberFormatException _) {
            System.out.println(INVALID_ID_MESSAGE);
            return -1;
        }
    }

    private static void listOrders() {
        List<OrdinazioneBean> orders = boundary.getAll();
        if (orders.isEmpty()) {
            System.out.println(NO_ORDERS_MESSAGE);
            return;
        }

        System.out.println("Lista Ordini:");
        orders.forEach(o -> System.out.println(
                o.getId() + " - " + o.getNome() + " (" + o.getTipoOrdine() + ")"
        ));
    }
}