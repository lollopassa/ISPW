package com.biteme.app.cli;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.boundary.PrenotazioneBoundary;
import com.biteme.app.exception.EmailSendingException;
import com.biteme.app.exception.PrenotationValidationException;
import com.biteme.app.util.CLIUtils;

public class PrenotazioneCLI {

    private PrenotazioneCLI() {
    }

    private static final PrenotazioneBoundary boundary = new PrenotazioneBoundary();
    private static final String PRENOTAZIONI_PER_DATA = "Prenotazioni per il ";

    public static void start() {
        var scanner = CLIUtils.getScanner();
        while (true) {
            showMenu();
            String scelta = scanner.nextLine();
            switch (scelta) {
                case "1":
                    creaPrenotazione(scanner);
                    break;
                case "2":
                    modificaPrenotazione(scanner);
                    break;
                case "3":
                    listaPrenotazioniPerData(scanner);
                    break;
                case "4":
                    inviaEmail(scanner);
                    break;
                case "5":
                    eliminaPrenotazione(scanner);
                    break;
                case "6":
                    return;
                default:
                    System.out.println("Opzione non valida.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("========== Prenotazioni CLI ==========");
        System.out.println("1. Crea Prenotazione");
        System.out.println("2. Modifica Prenotazione");
        System.out.println("3. Lista Prenotazioni per Data");
        System.out.println("4. Invia Email della Prenotazione");
        System.out.println("5. Elimina Prenotazione");
        System.out.println("6. Torna al Menu");
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
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Note: ");
        String note = scanner.nextLine().trim();

        PrenotazioneBean bean = new PrenotazioneBean();
        bean.setNomeCliente(nomeCliente);
        bean.setData(data);
        bean.setOrarioStr(orarioStr);
        bean.setCopertiStr(copertiStr);
        bean.setEmail(email);
        bean.setNote(note);

        try {
            boundary.creaPrenotazione(bean);
            System.out.println("Prenotazione creata con successo.");
        } catch (PrenotationValidationException e) {
            System.out.println("Errore nella creazione della prenotazione: " + e.getMessage());
        }
    }

    private static void eliminaPrenotazione(Scanner scanner) {
        System.out.print("Inserisci l'ID della prenotazione da eliminare: ");
        int id = Integer.parseInt(scanner.nextLine());
        try {
            boundary.eliminaPrenotazione(id);
            System.out.println("Prenotazione eliminata.");
        } catch (IllegalArgumentException e) {
            System.out.println("Errore nell'eliminazione della prenotazione: " + e.getMessage());
        }
    }

    private static void listaPrenotazioniPerData(Scanner scanner) {
        System.out.print("Inserisci la data (YYYY-MM-DD): ");
        LocalDate data = LocalDate.parse(scanner.nextLine().trim());
        List<PrenotazioneBean> prenotazioni = boundary.getPrenotazioniByData(data);
        if (prenotazioni.isEmpty()) {
            System.out.println("Nessuna prenotazione per la data specificata.");
        } else {
            System.out.println(PRENOTAZIONI_PER_DATA + data + ":");
            prenotazioni.forEach(p ->
                    System.out.println(p.getId() + " - " + p.getNomeCliente() + " alle " + p.getOrario()));
        }
    }

    private static void inviaEmail(Scanner scanner) {
        System.out.print("Inserisci la data (YYYY-MM-DD) della prenotazione: ");
        LocalDate data = LocalDate.parse(scanner.nextLine().trim());
        List<PrenotazioneBean> prenotazioni = boundary.getPrenotazioniByData(data);
        if (prenotazioni.isEmpty()) {
            System.out.println("Nessuna prenotazione trovata per la data specificata.");
            return;
        }
        System.out.println(PRENOTAZIONI_PER_DATA + data + ":");
        prenotazioni.forEach(p ->
                System.out.println(p.getId() + " - " + p.getNomeCliente() + " alle " + p.getOrario()));

        System.out.print("Inserisci l'ID della prenotazione per inviare l'email: ");
        int id = Integer.parseInt(scanner.nextLine());
        PrenotazioneBean selected = prenotazioni.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
        if (selected == null) {
            System.out.println("Prenotazione non trovata.");
            return;
        }

        System.out.print("Inserisci l'indirizzo email del cliente: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            System.out.println("L'indirizzo email non può essere vuoto.");
            return;
        }

        try {
            boundary.inviaEmail(selected, email);
            System.out.println("Email inviata correttamente a " + email);
        } catch (EmailSendingException e) {
            System.out.println("Errore durante l'invio dell'email: " + e.getMessage());
        }
    }

    private static void modificaPrenotazione(Scanner scanner) {
        System.out.print("Inserisci la data (YYYY-MM-DD) della prenotazione da modificare: ");
        String dataInput = scanner.nextLine().trim();
        LocalDate data;
        try {
            data = LocalDate.parse(dataInput);
        } catch (Exception e) {
            System.out.println("Formato data non valido. Assicurati di utilizzare il formato YYYY-MM-DD.");
            return;
        }
        List<PrenotazioneBean> prenotazioni = boundary.getPrenotazioniByData(data);
        if (prenotazioni.isEmpty()) {
            System.out.println("Nessuna prenotazione trovata per la data specificata.");
            return;
        }
        System.out.println(PRENOTAZIONI_PER_DATA + data + ":");
        prenotazioni.forEach(p -> System.out.println(p.getId() + " - " + p.getNomeCliente()));

        System.out.print("Inserisci l'ID della prenotazione da modificare: ");
        int id = Integer.parseInt(scanner.nextLine());
        PrenotazioneBean prenotazioneEsistente = prenotazioni.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
        if (prenotazioneEsistente == null) {
            System.out.println("Prenotazione non trovata.");
            return;
        }

        System.out.println("Lascia vuoto il campo per mantenere il valore attuale.");

        System.out.print("Nuovo Nome Cliente (attuale: " + prenotazioneEsistente.getNomeCliente() + "): ");
        String nomeCliente = scanner.nextLine().trim();
        if (nomeCliente.isEmpty()) nomeCliente = prenotazioneEsistente.getNomeCliente();

        System.out.print("Nuova Data (YYYY-MM-DD) (attuale: " + prenotazioneEsistente.getData() + "): ");
        String dataStr = scanner.nextLine().trim();
        if (dataStr.isEmpty()) dataStr = prenotazioneEsistente.getData().toString();
        LocalDate nuovaData = LocalDate.parse(dataStr);

        System.out.print("Nuovo Orario (HH:mm) (attuale: " + prenotazioneEsistente.getOrario() + "): ");
        String orarioStr = scanner.nextLine().trim();
        if (orarioStr.isEmpty()) orarioStr = prenotazioneEsistente.getOrario().toString();

        System.out.print("Nuovo Numero di Coperti (attuale: " + prenotazioneEsistente.getCoperti() + "): ");
        String copertiStr = scanner.nextLine().trim();
        if (copertiStr.isEmpty()) copertiStr = String.valueOf(prenotazioneEsistente.getCoperti());

        System.out.print("Nuova Email (attuale: " + prenotazioneEsistente.getEmail() + "): ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) email = prenotazioneEsistente.getEmail();

        System.out.print("Nuove Note (attuale: " + prenotazioneEsistente.getNote() + "): ");
        String note = scanner.nextLine().trim();
        if (note.isEmpty()) note = prenotazioneEsistente.getNote();

        PrenotazioneBean bean = new PrenotazioneBean();
        bean.setId(id);
        bean.setNomeCliente(nomeCliente);
        bean.setData(nuovaData);
        bean.setOrarioStr(orarioStr);
        bean.setCopertiStr(copertiStr);
        bean.setEmail(email);
        bean.setNote(note);

        try {
            PrenotazioneBean updated = boundary.modificaPrenotazione(bean);
            System.out.println("Prenotazione modificata con successo.");
            System.out.println("Dettagli aggiornati: ");
            System.out.println("ID: " + updated.getId());
            System.out.println("Nome Cliente: " + updated.getNomeCliente());
            System.out.println("Data: " + updated.getData());
            System.out.println("Orario: " + updated.getOrario());
            System.out.println("Coperti: " + updated.getCoperti());
            System.out.println("Email: " + updated.getEmail());
            System.out.println("Note: " + updated.getNote());
        } catch (PrenotationValidationException e) {
            System.out.println("Errore nella modifica della prenotazione: " + e.getMessage());
        }
    }
}