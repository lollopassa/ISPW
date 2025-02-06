package com.biteme.app.cli;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import com.biteme.app.bean.PrenotazioneBean;
import com.biteme.app.controller.PrenotazioneController;

import java.time.LocalTime;

public class PrenotazioneCLI {
    private static PrenotazioneController prenotazioneController = new PrenotazioneController();

    public static void start() {
        Scanner scanner = CLIUtils.getScanner();
        while (true) {
            System.out.println("========== Prenotazioni CLI ==========");
            System.out.println("1. Crea Prenotazione");
            System.out.println("2. Elimina Prenotazione");
            System.out.println("3. Lista Prenotazioni per Data");
            System.out.println("4. Torna al Menu");
            System.out.print("Scegli un'opzione: ");
            String scelta = scanner.nextLine();

            if (scelta.equals("1")) {
                PrenotazioneBean bean = new PrenotazioneBean();
                System.out.print("Nome Cliente: ");
                bean.setNomeCliente(scanner.nextLine().trim());
                System.out.print("Data (YYYY-MM-DD): ");
                bean.setData(LocalDate.parse(scanner.nextLine().trim()));
                System.out.print("Orario (HH:mm): ");
                bean.setOrario(LocalTime.parse(scanner.nextLine().trim()));
                System.out.print("Coperti: ");
                bean.setCoperti(Integer.parseInt(scanner.nextLine().trim()));
                System.out.print("Telefono: ");
                bean.setTelefono(scanner.nextLine().trim());
                System.out.print("Note: ");
                bean.setNote(scanner.nextLine().trim());
                prenotazioneController.creaPrenotazione(bean);
                System.out.println("Prenotazione creata con successo.");
            } else if (scelta.equals("2")) {
                System.out.print("Inserisci l'ID della prenotazione da eliminare: ");
                int id = Integer.parseInt(scanner.nextLine());
                prenotazioneController.eliminaPrenotazione(id);
                System.out.println("Prenotazione eliminata.");
            } else if (scelta.equals("3")) {
                System.out.print("Inserisci la data (YYYY-MM-DD): ");
                LocalDate data = LocalDate.parse(scanner.nextLine().trim());
                List<PrenotazioneBean> prenotazioni = prenotazioneController.getPrenotazioniByData(data);
                if (prenotazioni.isEmpty()) {
                    System.out.println("Nessuna prenotazione per la data specificata.");
                } else {
                    System.out.println("Prenotazioni per il " + data + ":");
                    for (PrenotazioneBean p : prenotazioni) {
                        System.out.println(p.getId() + " - " + p.getNomeCliente() + " alle " + p.getOrario());
                    }
                }
            } else if (scelta.equals("4")) {
                break;
            } else {
                System.out.println("Opzione non valida.");
            }
        }
    }
}
