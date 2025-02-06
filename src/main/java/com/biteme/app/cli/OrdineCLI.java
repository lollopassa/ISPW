package com.biteme.app.cli;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.OrdineController;
import com.biteme.app.controller.ProdottoController;

import java.util.List;
import java.util.Scanner;

public class OrdineCLI {
    // Utilizza i controller per lavorare con gli ordini e i prodotti
    private static OrdineController ordineController = new OrdineController();
    private static ProdottoController prodottoController = new ProdottoController();

    public static void start(int orderId) {
        Scanner scanner = new Scanner(System.in);
        // Recupera l'OrdineBean utilizzando l'ID
        OrdineBean ordineBean;
        try {
            ordineBean = ordineController.getOrdineById(orderId);
        } catch (Exception e) {
            System.out.println("Ordine non trovato: " + e.getMessage());
            return;
        }

        // Se le liste non sono inizializzate oppure sono immutabili, creale come nuove ArrayList
        if (ordineBean.getProdotti() == null) {
            ordineBean.setProdotti(new java.util.ArrayList<>());
        } else {
            // Crea una copia mutabile
            ordineBean.setProdotti(new java.util.ArrayList<>(ordineBean.getProdotti()));
        }
        if (ordineBean.getQuantita() == null) {
            ordineBean.setQuantita(new java.util.ArrayList<>());
        } else {
            // Crea una copia mutabile
            ordineBean.setQuantita(new java.util.ArrayList<>(ordineBean.getQuantita()));
        }

        boolean modificando = true;
        while (modificando) {
            System.out.println("----- Modifica Ordine (ID: " + orderId + ") -----");
            mostraOrdine(ordineBean);
            System.out.println("Opzioni:");
            System.out.println("1. Aggiungi prodotto");
            System.out.println("2. Rimuovi prodotto");
            System.out.println("3. Modifica quantità di un prodotto");
            System.out.println("4. Salva modifiche e torna indietro");
            System.out.println("5. Annulla modifiche e torna indietro");
            System.out.print("Scegli un'opzione: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    aggiungiProdotto(scanner, ordineBean);
                    break;
                case "2":
                    rimuoviProdotto(scanner, ordineBean);
                    break;
                case "3":
                    modificaQuantita(scanner, ordineBean);
                    break;
                case "4":
                    // Salva le modifiche chiamando il metodo del controller
                    ordineController.salvaOrdine(ordineBean, orderId);
                    System.out.println("Modifiche salvate.");
                    modificando = false;
                    break;
                case "5":
                    System.out.println("Modifiche annullate.");
                    modificando = false;
                    break;
                default:
                    System.out.println("Opzione non valida.");
                    break;
            }
        }
    }


    private static void mostraOrdine(OrdineBean ordineBean) {
        List<String> prodotti = ordineBean.getProdotti();
        List<Integer> quantita = ordineBean.getQuantita();

        if (prodotti.isEmpty()) {
            System.out.println("L'ordine è vuoto.");
        } else {
            System.out.println("Prodotti attuali:");
            for (int i = 0; i < prodotti.size(); i++) {
                String nomeProdotto = prodotti.get(i);
                int qta = quantita.get(i);

                // Recupera i dettagli del prodotto tramite il controller
                ProdottoBean prodotto = prodottoController.getProdottoByNome(nomeProdotto);
                if (prodotto != null) {
                    System.out.println((i + 1) + ". "
                            + prodotto.getId() + " - "
                            + prodotto.getNome() + " - "
                            + prodotto.getCategoria() + " - "
                            + prodotto.getPrezzo() + " € - "
                            + qta + " pezzi");
                } else {
                    System.out.println((i + 1) + ". " + nomeProdotto + " - Dettagli non disponibili - " + qta + " pezzi");
                }
            }
        }
    }


    private static void aggiungiProdotto(Scanner scanner, OrdineBean ordineBean) {
        System.out.print("Inserisci il nome del prodotto da aggiungere: ");
        String nomeProdottoInput = scanner.nextLine();
        // Normalizza l'input: rimuove spazi superflui
        String nomeProdotto = nomeProdottoInput.trim();

        // Se necessario, puoi forzare il formato (ad es. toUpperCase) in base a come sono memorizzati i nomi nel database:
        // nomeProdotto = nomeProdotto.toUpperCase();

        // Recupera il ProdottoBean tramite il ProdottoController
        ProdottoBean prodotto = prodottoController.getProdottoByNome(nomeProdotto);
        if (prodotto == null) {
            System.out.println("Prodotto non trovato per il nome: " + nomeProdotto);
            return;
        }

        System.out.print("Inserisci la quantità: ");
        try {
            int qty = Integer.parseInt(scanner.nextLine());
            if (qty <= 0) {
                System.out.println("La quantità deve essere maggiore di zero.");
                return;
            }
            // Aggiungi il nome del prodotto (ottenuto dal ProdottoBean) nell'OrdineBean
            ordineBean.getProdotti().add(prodotto.getNome());
            ordineBean.getQuantita().add(qty);
            System.out.println("Prodotto aggiunto: " + prodotto.getNome() + " - Quantità: " + qty);
        } catch (NumberFormatException e) {
            System.out.println("Quantità non valida.");
        }
    }

    private static void rimuoviProdotto(Scanner scanner, OrdineBean ordineBean) {
        List<String> prodotti = ordineBean.getProdotti();
        if (prodotti.isEmpty()) {
            System.out.println("Non ci sono prodotti da rimuovere.");
            return;
        }
        System.out.print("Inserisci il numero del prodotto da rimuovere (vedi lista): ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            if (index >= 0 && index < prodotti.size()) {
                String removed = prodotti.remove(index);
                ordineBean.getQuantita().remove(index);
                System.out.println("Rimosso " + removed + " dall'ordine.");
            } else {
                System.out.println("Indice non valido.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non valido.");
        }
    }

    private static void modificaQuantita(Scanner scanner, OrdineBean ordineBean) {
        List<String> prodotti = ordineBean.getProdotti();
        if (prodotti.isEmpty()) {
            System.out.println("Non ci sono prodotti da modificare.");
            return;
        }
        System.out.print("Inserisci il numero del prodotto da modificare (vedi lista): ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            if (index >= 0 && index < prodotti.size()) {
                System.out.print("Inserisci la nuova quantità: ");
                int newQty = Integer.parseInt(scanner.nextLine());
                if (newQty < 0) {
                    System.out.println("La quantità non può essere negativa.");
                    return;
                }
                ordineBean.getQuantita().set(index, newQty);
                System.out.println("Quantità aggiornata.");
            } else {
                System.out.println("Indice non valido.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input non valido.");
        }
    }
}

