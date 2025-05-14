package com.biteme.app.cli;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.boundary.OrdineBoundary;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.util.CLIUtils;

import java.util.List;
import java.util.Scanner;

public class OrdineCLI {

    private OrdineCLI() {}

    private static final OrdineBoundary boundary = new OrdineBoundary();
    private static final Scanner scanner = CLIUtils.getScanner();

    public static void start(int orderId) throws OrdineException {
        OrdineBean ordineBean;
        try {
            ordineBean = boundary.loadOrdine(orderId);
        } catch (OrdineException e) {
            System.out.println("Ordine non trovato: " + e.getMessage());
            return;
        }

        if (ordineBean.getProdotti() == null) {
            ordineBean.setProdotti(new java.util.ArrayList<>());
        } else {
            ordineBean.setProdotti(new java.util.ArrayList<>(ordineBean.getProdotti()));
        }
        if (ordineBean.getQuantita() == null) {
            ordineBean.setQuantita(new java.util.ArrayList<>());
        } else {
            ordineBean.setQuantita(new java.util.ArrayList<>(ordineBean.getQuantita()));
        }

        boolean editing = true;
        while (editing) {
            System.out.println("----- Modifica Ordine (ID: " + orderId + ") -----");
            mostraOrdine(ordineBean);
            System.out.println("Opzioni:");
            System.out.println("1. Aggiungi prodotto");
            System.out.println("2. Rimuovi prodotto");
            System.out.println("3. Modifica quantità");
            System.out.println("4. Salva modifiche e torna indietro");
            System.out.println("5. Annulla modifiche e torna indietro");
            System.out.print("Scegli un'opzione: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> aggiungiProdotto(ordineBean);
                case "2" -> rimuoviProdotto(ordineBean);
                case "3" -> modificaQuantita(ordineBean);
                case "4" -> {
                    try {
                        boundary.salvaOrdineCompleto(orderId, ordineBean);
                        System.out.println("Modifiche salvate.");
                    } catch (OrdineException e) {
                        System.out.println("Errore nel salvataggio: " + e.getMessage());
                    }
                    editing = false;
                }
                case "5" -> {
                    System.out.println("Modifiche annullate.");
                    editing = false;
                }
                default -> System.out.println("Opzione non valida.");
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
                System.out.println((i + 1) + ". " + nomeProdotto + " - Quantità: " + qta);
            }
        }
    }

    private static void aggiungiProdotto(OrdineBean ordineBean) {
        System.out.print("Nome prodotto: ");
        String nome = scanner.nextLine().trim();
        System.out.print("Quantità: ");
        int qty;
        try {
            qty = Integer.parseInt(scanner.nextLine());
            if (qty <= 0) {
                System.out.println("La quantità deve essere >0.");
                return;
            }
        } catch (NumberFormatException _) {
            System.out.println("Quantità non valida.");
            return;
        }
        ordineBean.getProdotti().add(nome);
        ordineBean.getQuantita().add(qty);
        System.out.println("Prodotto aggiunto: " + nome + " x" + qty);
    }

    private static void rimuoviProdotto(OrdineBean ordineBean) {
        if (ordineBean.getProdotti().isEmpty()) {
            System.out.println("Nessun prodotto da rimuovere.");
            return;
        }
        System.out.print("Numero prodotto da rimuovere: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx < 0 || idx >= ordineBean.getProdotti().size()) {
                System.out.println("Indice non valido.");
                return;
            }
            String removed = ordineBean.getProdotti().remove(idx);
            ordineBean.getQuantita().remove(idx);
            System.out.println("Rimosso: " + removed);
        } catch (NumberFormatException _) {
            System.out.println("Input non valido.");
        }
    }

    private static void modificaQuantita(OrdineBean ordineBean) {
        if (ordineBean.getProdotti().isEmpty()) {
            System.out.println("Nessun prodotto da modificare.");
            return;
        }
        System.out.print("Numero prodotto da modificare: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            System.out.print("Nuova quantità: ");
            int newQty = Integer.parseInt(scanner.nextLine());
            if (newQty < 0) {
                System.out.println("La quantità non può essere negativa.");
                return;
            }
            ordineBean.getQuantita().set(idx, newQty);
            System.out.println("Quantità aggiornata.");
        } catch (Exception _) {
            System.out.println("Input non valido.");
        }
    }
}
