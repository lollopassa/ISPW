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

    private OrdinazioneCLI() {
    //costruttore privato
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

        System.out.print("Tipo Ordine (AL_TAVOLO/ASPORTO): ");
        bean.setTipoOrdine(com.biteme.app.model.TipoOrdine.valueOf(SCANNER.nextLine()));

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
        if (id != -1) OrdineCLI.start(id);
    }

    private static void handleDeleteOrder() {
        List<OrdinazioneBean> orders = ORDINAZIONE_CONTROLLER.getOrdini();
        if (showOrderList(orders)) return;

        int id = promptForOrderId();
        if (id != -1) {
            ORDINAZIONE_CONTROLLER.eliminaOrdine(id);
            System.out.println("Ordine eliminato.");
        }
    }

    private static void handleArchiveOrder() {
        List<OrdinazioneBean> orders = ORDINAZIONE_CONTROLLER.getOrdini();
        if (showOrderList(orders)) return;

        int id = promptForOrderId();
        if (id != -1) {
            try {
                ARCHIVIO_CONTROLLER.archiviaOrdine(new com.biteme.app.bean.ArchivioBean());
                ORDINAZIONE_CONTROLLER.eliminaOrdine(id);
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