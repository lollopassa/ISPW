package com.biteme.app.cli;

import java.time.LocalDate;
import java.util.List;
import com.biteme.app.bean.PrenotazioniBean;
import com.biteme.app.controller.PrenotazioniController;
import com.biteme.app.model.Prenotazione;
import java.time.LocalTime;

public class PrenotazioniCLI {
    private static PrenotazioniController prenotazioniController = new PrenotazioniController();

    public static void start() {
        var scanner = CLIUtils.getScanner();
        while(true) {
            System.out.println("========== Prenotazioni CLI ==========");
            System.out.println("1. Crea Prenotazione");
            System.out.println("2. Elimina Prenotazione");
            System.out.println("3. Lista Prenotazioni per Data");
            System.out.println("4. Torna al Menu");
            System.out.print("Scegli un'opzione: ");
            String scelta = scanner.nextLine();

            if(scelta.equals("1")) {
                PrenotazioniBean bean = new PrenotazioniBean();
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
                prenotazioniController.creaPrenotazione(bean);
                System.out.println("Prenotazione creata con successo.");
            } else if(scelta.equals("2")) {
                System.out.print("Inserisci l'ID della prenotazione da eliminare: ");
                int id = Integer.parseInt(scanner.nextLine());
                prenotazioniController.eliminaPrenotazione(id);
                System.out.println("Prenotazione eliminata.");
            } else if(scelta.equals("3")) {
                System.out.print("Inserisci la data (YYYY-MM-DD): ");
                LocalDate data = LocalDate.parse(scanner.nextLine().trim());
                List<Prenotazione> prenotazioni = prenotazioniController.getPrenotazioniByData(data);
                if(prenotazioni.isEmpty()) {
                    System.out.println("Nessuna prenotazione per la data specificata.");
                } else {
                    System.out.println("Prenotazioni per il " + data + ":");
                    for(Prenotazione p : prenotazioni) {
                        System.out.println(p.getId() + " - " + p.getNomeCliente() + " alle " + p.getOrario());
                    }
                }
            } else if(scelta.equals("4")) {
                break;
            } else {
                System.out.println("Opzione non valida.");
            }
        }
    }
}
