package com.biteme.app.cli;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import com.biteme.app.bean.EmailBean;
import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.controller.EmailController;
import com.biteme.app.controller.PrenotazioneController;

public class PrenotazioneCLI {

    // Costruttore privato per evitare istanziazioni
    private PrenotazioneCLI() {
    }

    private static final PrenotazioneController prenotazioneController = new PrenotazioneController();
    private static final EmailController emailController = new EmailController();

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
                    inviaEmail(scanner);
                    break;
                case "5":
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
        System.out.println("4. Invia Email della Prenotazione");
        System.out.println("5. Torna al Menu");
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
            prenotazioneController.creaPrenotazione(
                    nomeCliente, orarioStr, data, telefono, note, copertiStr);
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
            prenotazioni.forEach(p ->
                    System.out.println(p.getId() + " - " + p.getNomeCliente() + " alle " + p.getOrario()));
        }
    }

    private static void inviaEmail(Scanner scanner) {
        // Chiede la data della prenotazione e visualizza le prenotazioni per quella data
        System.out.print("Inserisci la data (YYYY-MM-DD) della prenotazione: ");
        LocalDate data = LocalDate.parse(scanner.nextLine().trim());
        List<PrenotazioneBean> prenotazioni = prenotazioneController.getPrenotazioniByData(data);
        if (prenotazioni.isEmpty()) {
            System.out.println("Nessuna prenotazione trovata per la data specificata.");
            return;
        }
        System.out.println("Prenotazioni per il " + data + ":");
        for (PrenotazioneBean p : prenotazioni) {
            System.out.println(p.getId() + " - " + p.getNomeCliente() + " alle " + p.getOrario());
        }
        // Selezione della prenotazione tramite ID
        System.out.print("Inserisci l'ID della prenotazione per inviare l'email: ");
        int id = Integer.parseInt(scanner.nextLine());
        PrenotazioneBean selected = null;
        for (PrenotazioneBean p : prenotazioni) {
            if (p.getId() == id) {
                selected = p;
                break;
            }
        }
        if (selected == null) {
            System.out.println("Prenotazione non trovata.");
            return;
        }
        // Inserimento dell'indirizzo email
        System.out.print("Inserisci l'indirizzo email del cliente: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            System.out.println("L'indirizzo email non puÃ² essere vuoto.");
            return;
        }
        // Composizione e invio dell'email tramite Gmail
        EmailBean emailBean = emailController.composeEmailFromPrenotazione(selected);
        emailBean.setDestinatario(email);
        try {
            emailController.sendEmail(emailBean);
            System.out.println("Email inviata correttamente a " + email);
        } catch (Exception e) {
            System.out.println("Errore durante l'invio dell'email: " + e.getMessage());
        }
    }
}