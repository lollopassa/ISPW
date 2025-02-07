package com.biteme.app.cli;

import java.util.List;
import java.util.Scanner;

import com.biteme.app.bean.OrdinazioneBean;
import com.biteme.app.controller.ArchivioController;
import com.biteme.app.controller.OrdinazioneController;

public class OrdinazioneCLI {
    private static final OrdinazioneController ORDINAZIONE_CONTROLLER = new OrdinazioneController();
    private static final ArchivioController ARCHIVIO_CONTROLLER = new ArchivioController();
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

    // Costruttore privato per evitare istanziazioni
    private OrdinazioneCLI() {
    }

    public static void start() {
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
        OrdinazioneBean bean = new OrdinazioneBean();

        System.out.print("Nome Cliente: ");
        bean.setNome(SCANNER.nextLine());

        System.out.print("Tipo Ordine (Al Tavolo/Asporto): ");
        String inputTipo = SCANNER.nextLine().trim();
        // Convertiamo l'input (eventualmente in maiuscolo) nel formato atteso dal bean
        if (inputTipo.equalsIgnoreCase("AL_TAVOLO") || inputTipo.equalsIgnoreCase("Al Tavolo")) {
            bean.setTipoOrdine("Al Tavolo");
        } else if (inputTipo.equalsIgnoreCase("ASPORTO") || inputTipo.equalsIgnoreCase("Asporto")) {
            bean.setTipoOrdine("Asporto");
        } else {
            System.out.println("Tipo Ordine non valido. Operazione annullata.");
            return;
        }

        System.out.print("Orario (HH:mm): ");
        bean.setOrarioCreazione(SCANNER.nextLine());

        System.out.print("Coperti: ");
        bean.setNumeroClienti(SCANNER.nextLine());

        System.out.print("Info Tavolo (se applicabile): ");
        bean.setInfoTavolo(SCANNER.nextLine());

        ORDINAZIONE_CONTROLLER.creaOrdine(bean);
        System.out.println("Ordine creato con successo.");
    }

    private static void handleModifyOrder() {
        List<OrdinazioneBean> orders = ORDINAZIONE_CONTROLLER.getOrdini();
        if (showOrderList(orders)) return;

        int id = promptForOrderId();
        if (id != -1) {
            // Avvia la CLI di modifica (presumibilmente OrdineCLI è l'interfaccia per la modifica)
            OrdineCLI.start(id);
        }
    }

    private static void handleDeleteOrder() {
        List<OrdinazioneBean> orders = ORDINAZIONE_CONTROLLER.getOrdini();
        if (showOrderList(orders)) return;

        int id = promptForOrderId();
        if (id != -1) {
            ORDINAZIONE_CONTROLLER.eliminaOrdinazione(id);
            System.out.println("Ordine eliminato.");
        }
    }

    private static void handleArchiveOrder() {
        List<OrdinazioneBean> orders = ORDINAZIONE_CONTROLLER.getOrdini();
        if (showOrderList(orders)) return;

        int id = promptForOrderId();
        if (id != -1) {
            try {
                // Se necessario, recupera e compila i dati dell'ArchivioBean (qui viene passato un bean vuoto)
                ARCHIVIO_CONTROLLER.archiviaOrdine(new com.biteme.app.bean.ArchivioBean());
                ORDINAZIONE_CONTROLLER.eliminaOrdinazione(id);
                System.out.println("Ordine archiviato con successo.");
            } catch (Exception e) {
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
        } catch (NumberFormatException e) {
            System.out.println(INVALID_ID_MESSAGE);
            return -1;
        }
    }

    private static void listOrders() {
        List<OrdinazioneBean> orders = ORDINAZIONE_CONTROLLER.getOrdini();
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
