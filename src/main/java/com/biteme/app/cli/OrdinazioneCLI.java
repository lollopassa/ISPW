package com.biteme.app.cli;

import java.util.List;
import java.util.Scanner;

import com.biteme.app.controller.ArchivioController;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.controller.OrdinazioneController;
import com.biteme.app.model.Ordinazione;

public class OrdinazioneCLI {

    private static OrdinazioneController ordinazioneController = new OrdinazioneController();
    private static OrdineController ordineController = new OrdineController();
    private static ArchivioController archivioController = new ArchivioController();

    public static void start() {
        Scanner scanner = CLIUtils.getScanner();
        boolean exit = false;
        while (!exit) {
            System.out.println("========== Ordinazione CLI ==========");
            System.out.println("1. Crea Ordine");
            System.out.println("2. Modifica Ordine");
            System.out.println("3. Elimina Ordine");
            System.out.println("4. Archivia Ordine");
            System.out.println("5. Lista Ordini");
            System.out.println("6. Torna al Menu");
            System.out.print("Scegli un'opzione: ");
            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    System.out.print("Nome Cliente: ");
                    String nome = scanner.nextLine();
                    System.out.print("Tipo Ordine (AL_TAVOLO / ASPORTO): ");
                    String tipoOrdineStr = scanner.nextLine();
                    System.out.print("Orario (HH:mm): ");
                    String orario = scanner.nextLine();
                    System.out.print("Coperti: ");
                    String coperti = scanner.nextLine();
                    System.out.print("Info Tavolo (se applicabile): ");
                    String tavolo = scanner.nextLine();

                    com.biteme.app.bean.OrdinazioneBean bean = new com.biteme.app.bean.OrdinazioneBean();
                    bean.setNome(nome);
                    bean.setTipoOrdine(com.biteme.app.model.TipoOrdine.valueOf(tipoOrdineStr));
                    bean.setOrarioCreazione(orario);
                    bean.setNumeroClienti(coperti);
                    bean.setInfoTavolo(tavolo);

                    ordinazioneController.creaOrdine(bean);
                    System.out.println("Ordine creato con successo.");
                    break;

                case "2":
                    List<Ordinazione> ordini = ordinazioneController.getOrdini();
                    if (ordini.isEmpty()) {
                        System.out.println("Nessun ordine disponibile.");
                    } else {
                        for (Ordinazione o : ordini) {
                            System.out.println(o.getId() + " - " + o.getNomeCliente());
                        }
                        System.out.print("Inserisci l'ID dell'ordine da modificare: ");
                        try {
                            int id = Integer.parseInt(scanner.nextLine());
                            // Avvia OrdineCLI per modificare l'ordine selezionato
                            OrdineCLI.start(id);
                        } catch (NumberFormatException e) {
                            System.out.println("ID non valido.");
                        }
                    }
                    break;

                case "3":
                    List<Ordinazione> ordiniDaEliminare = ordinazioneController.getOrdini();
                    if (ordiniDaEliminare.isEmpty()) {
                        System.out.println("Nessun ordine disponibile.");
                    } else {
                        for (Ordinazione o : ordiniDaEliminare) {
                            System.out.println(o.getId() + " - " + o.getNomeCliente());
                        }
                        System.out.print("Inserisci l'ID dell'ordine da eliminare: ");
                        try {
                            int id = Integer.parseInt(scanner.nextLine());
                            ordinazioneController.eliminaOrdine(id);
                            System.out.println("Ordine eliminato.");
                        } catch (NumberFormatException e) {
                            System.out.println("ID non valido.");
                        }
                    }
                    break;

                case "4":
                    List<Ordinazione> ordiniDaArchiviare = ordinazioneController.getOrdini();
                    if (ordiniDaArchiviare.isEmpty()) {
                        System.out.println("Nessun ordine disponibile.");
                    } else {
                        for (Ordinazione o : ordiniDaArchiviare) {
                            System.out.println(o.getId() + " - " + o.getNomeCliente());
                        }
                        System.out.print("Inserisci l'ID dell'ordine da archiviare: ");
                        try {
                            int id = Integer.parseInt(scanner.nextLine());
                            try {
                                var ordineBean = ordineController.getOrdineById(id);
                                archivioController.archiviaOrdine(new com.biteme.app.bean.ArchivioBean());
                                ordinazioneController.eliminaOrdine(id);
                                System.out.println("Ordine archiviato con successo.");
                            } catch (Exception e) {
                                System.out.println("Errore durante l'archiviazione: " + e.getMessage());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("ID non valido.");
                        }
                    }
                    break;

                case "5":
                    List<Ordinazione> listaOrdini = ordinazioneController.getOrdini();
                    if (listaOrdini.isEmpty()) {
                        System.out.println("Nessun ordine disponibile.");
                    } else {
                        System.out.println("Lista Ordini:");
                        for (Ordinazione o : listaOrdini) {
                            System.out.println(o.getId() + " - " + o.getNomeCliente() + " (" + o.getTipoOrdine() + ")");
                        }
                    }
                    break;

                case "6":
                    exit = true;
                    break;

                default:
                    System.out.println("Opzione non valida.");
                    break;
            }
        }
    }
}
