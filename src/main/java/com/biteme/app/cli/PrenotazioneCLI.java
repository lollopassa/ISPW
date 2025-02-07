package com.biteme.app.cli;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.controller.PrenotazioneController;

public class PrenotazioneCLI {

    private PrenotazioneCLI() {
        //costruttore privato
    }

    private static final PrenotazioneController prenotazioneController = new PrenotazioneController();

    public static void start() {
        Scanner scanner = CLIUtils.getScanner();
        while (true) {
            showMenu();
            String scelta = scanner.nextLine();
            switch (scelta) {
                case "1":
                    creaPrenotazione(scanner);
                    break;
                case "2":
                    eliminaPrenotazione(scanner);
                    break;
                case "3":
                    listaPrenotazioniPerData(scanner);
                    break;
                case "4":
                    return; // Esci dal ciclo
                default:
                    System.out.println("Opzione non valida.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("========== Prenotazioni CLI ==========");
        System.out.println("1. Crea Prenotazione");
        System.out.println("2. Elimina Prenotazione");
        System.out.println("3. Lista Prenotazioni per Data");
        System.out.println("4. Torna al Menu");
        System.out.print("Scegli un'opzione: ");
    }

    private static void creaPrenotazione(Scanner scanner) {
        System.out.print("Nome Cliente: ");
        String nomeCliente = scanner.nextLine().trim();
        System.out.print("Data (YYYY-MM-DD): ");
        LocalDate data = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Orario (HH:mm): ");
        String orarioStr = scanner.nextLine().trim();
        System.out.print("Coperti: ");
        String copertiStr = scanner.nextLine().trim();
        System.out.print("Telefono: ");
        String telefono = scanner.nextLine().trim();
        System.out.print("Note: ");
        String note = scanner.nextLine().trim();

        try {
            prenotazioneController.creaPrenotazione(nomeCliente, orarioStr, data, telefono, note, copertiStr);
            System.out.println("Prenotazione creata con successo.");
        } catch (Exception e) {
            System.out.println("Errore nella creazione della prenotazione: " + e.getMessage());
        }
    }

    private static void eliminaPrenotazione(Scanner scanner) {
        System.out.print("Inserisci l'ID della prenotazione da eliminare: ");
        int id = Integer.parseInt(scanner.nextLine());
        try {
            prenotazioneController.eliminaPrenotazione(id);
            System.out.println("Prenotazione eliminata.");
        } catch (Exception e) {
            System.out.println("Errore nell'eliminazione della prenotazione: " + e.getMessage());
        }
    }

    private static void listaPrenotazioniPerData(Scanner scanner) {
        System.out.print("Inserisci la data (YYYY-MM-DD): ");
        LocalDate data = LocalDate.parse(scanner.nextLine().trim());
        List<PrenotazioneBean> prenotazioni = prenotazioneController.getPrenotazioniByData(data);
        if (prenotazioni.isEmpty()) {
            System.out.println("Nessuna prenotazione per la data specificata.");
        } else {
            System.out.println("Prenotazioni per il " + data + ":");
            prenotazioni.forEach(p -> System.out.println(p.getId() + " - " + p.getNomeCliente() + " alle " + p.getOrario()));
        }
    }
}
