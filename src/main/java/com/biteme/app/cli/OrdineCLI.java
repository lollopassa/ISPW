package com.biteme.app.cli;

import com.biteme.app.bean.OrdineBean;
import com.biteme.app.boundary.OrdineBoundary;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.util.CLIUtils;

import java.util.List;
import java.util.Scanner;

public  class OrdineCLI {

    private OrdineCLI() {}

    private static  OrdineBoundary boundary = new OrdineBoundary();
    private static final Scanner        scanner  = CLIUtils.getScanner();

    public static void start(int ordineId) {
        OrdineBean ordineBean;
        try {
            ordineBean = boundary.getOrdine(ordineId);       // nuovo metodo
        } catch (OrdineException e) {
            System.out.println("Ordine non trovato: " + e.getMessage());
            return;
        }

        /* conversione a liste mutabili */
        ordineBean.setProdotti(
                ordineBean.getProdotti() == null ? new java.util.ArrayList<>() :
                        new java.util.ArrayList<>(ordineBean.getProdotti()));
        ordineBean.setQuantita(
                ordineBean.getQuantita() == null ? new java.util.ArrayList<>() :
                        new java.util.ArrayList<>(ordineBean.getQuantita()));

        boolean editing = true;
        while (editing) {
            System.out.println("\n----- Modifica Ordine (ID: " + ordineId + ") -----");
            mostraOrdine(ordineBean);
            System.out.println("""
                    Opzioni:
                    1. Aggiungi prodotto
                    2. Rimuovi prodotto
                    3. Modifica quantità
                    4. Salva modifiche e torna indietro
                    5. Annulla modifiche e torna indietro""");
            System.out.print("Scegli un'opzione: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> aggiungiProdotto(ordineBean);
                case "2" -> rimuoviProdotto(ordineBean);
                case "3" -> modificaQuantita(ordineBean);
                case "4" -> {
                    try {
                        boundary.salvaOrdineCompleto(
                                ordineId,
                                ordineBean.getProdotti(),
                                ordineBean.getQuantita(),
                                null                     // prezzi: li calcola il controller
                        );
                        System.out.println("Modifiche salvate.");
                    } catch (OrdineException e) {
                        System.out.println("Errore salvataggio: " + e.getMessage());
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

    /* ---------------- helper di visualizzazione ---------------- */

    private static void mostraOrdine(OrdineBean ob) {
        List<String>  prod = ob.getProdotti();
        List<Integer> qty  = ob.getQuantita();
        if (prod == null || prod.isEmpty()) {
            System.out.println("L'ordine è vuoto.");
            return;
        }
        for (int i = 0; i < prod.size(); i++) {
            System.out.printf("%d. %s - Quantità: %d%n", i + 1, prod.get(i), qty.get(i));
        }
    }

    /* ---------------- opzioni di editing ---------------- */

    private static void aggiungiProdotto(OrdineBean ob) {
        System.out.print("Nome prodotto: ");
        String nome = scanner.nextLine().trim();
        System.out.print("Quantità: ");
        int q;
        try {
            q = Integer.parseInt(scanner.nextLine());
            if (q <= 0) { System.out.println("La quantità deve essere >0."); return; }
        } catch (NumberFormatException _) {
            System.out.println("Quantità non valida."); return;
        }
        ob.getProdotti().add(nome);
        ob.getQuantita().add(q);
        System.out.println("Aggiunto: " + nome + " x" + q);
    }

    private static void rimuoviProdotto(OrdineBean ob) {
        if (ob.getProdotti().isEmpty()) {
            System.out.println("Nessun prodotto da rimuovere."); return;
        }
        System.out.print("Indice prodotto da rimuovere: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            if (idx < 0 || idx >= ob.getProdotti().size()) {
                System.out.println("Indice non valido."); return;
            }
            String removed = ob.getProdotti().remove(idx);
            ob.getQuantita().remove(idx);
            System.out.println("Rimosso: " + removed);
        } catch (NumberFormatException _) {
            System.out.println("Input non valido.");
        }
    }

    private static void modificaQuantita(OrdineBean ob) {
        if (ob.getProdotti().isEmpty()) {
            System.out.println("Nessun prodotto da modificare."); return;
        }
        try {
            System.out.print("Indice prodotto da modificare: ");
            int idx = Integer.parseInt(scanner.nextLine()) - 1;
            System.out.print("Nuova quantità: ");
            int newQ = Integer.parseInt(scanner.nextLine());
            if (newQ < 0) { System.out.println("Quantità negativa non ammessa."); return; }
            ob.getQuantita().set(idx, newQ);
            System.out.println("Quantità aggiornata.");
        } catch (Exception _) {
            System.out.println("Input non valido.");
        }
    }
}
