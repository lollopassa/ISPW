package com.biteme.app.cli;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.boundary.ProdottoBoundary;
import com.biteme.app.exception.ProdottoException;
import com.biteme.app.util.CLIUtils;

public class ProdottoCLI {

    private ProdottoCLI() {}

    private static final ProdottoBoundary boundary = new ProdottoBoundary();
    private static final Scanner scanner = CLIUtils.getScanner();
    private static final String ERROR_PREFIX = "Errore: ";

    public static void start() {
        boolean isAdmin = boundary.isUserAdmin();
        boolean running = true;

        while (running) {
            System.out.println("========== Prodotto CLI ==========");
            if (isAdmin) {
                running = handleAdminOptions();
            } else {
                running = handleUserOptions();
            }
        }
    }

    private static boolean handleAdminOptions() {
        System.out.println("1. Aggiungi Prodotto");
        System.out.println("2. Modifica Prodotto");
        System.out.println("3. Elimina Prodotto");
        System.out.println("4. Lista Prodotti");
        System.out.println("5. Torna al Menu");
        System.out.print("Scegli un'opzione: ");
        String scelta = scanner.nextLine();

        switch (scelta) {
            case "1" -> aggiungiProdotto();
            case "2" -> modificaProdotto();
            case "3" -> eliminaProdotto();
            case "4" -> listaProdotti();
            case "5" -> {
                return false;
            }
            default -> System.out.println("Opzione non valida.");
        }
        return true;
    }

    private static boolean handleUserOptions() {
        System.out.println("1. Lista Prodotti");
        System.out.println("2. Torna al Menu");
        System.out.print("Scegli un'opzione: ");
        String scelta = scanner.nextLine();

        switch (scelta) {
            case "1" -> listaProdotti();
            case "2" -> {
                return false;
            }
            default -> System.out.println("Opzione non valida.");
        }
        return true;
    }

    private static void aggiungiProdotto() {
        try {
            System.out.print("Nome Prodotto: ");
            String nome = scanner.nextLine();
            System.out.print("Categoria (PIZZE, PRIMI, ANTIPASTI, BEVANDE, CONTORNI, DOLCI): ");
            String categoria = scanner.nextLine().toUpperCase();
            System.out.print("Prezzo: ");
            BigDecimal prezzo = new BigDecimal(scanner.nextLine());

            ProdottoBean bean = new ProdottoBean();
            bean.setNome(nome);
            bean.setCategoria(categoria);
            bean.setPrezzo(prezzo);
            bean.setDisponibile(true);

            boundary.aggiungiProdotto(bean);
            System.out.println("Prodotto aggiunto correttamente.");
        } catch (ProdottoException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "Prezzo non valido.");
        }
    }

    private static void modificaProdotto() {
        try {
            System.out.print("ID Prodotto: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Nuovo Nome: ");
            String nome = scanner.nextLine();
            System.out.print("Nuova Categoria: ");
            String categoria = scanner.nextLine().toUpperCase();
            System.out.print("Nuovo Prezzo: ");
            BigDecimal prezzo = new BigDecimal(scanner.nextLine());

            ProdottoBean bean = new ProdottoBean();
            bean.setId(id);
            bean.setNome(nome);
            bean.setCategoria(categoria);
            bean.setPrezzo(prezzo);
            bean.setDisponibile(true);

            boundary.modificaProdotto(bean);
            System.out.println("Prodotto aggiornato con successo.");
        } catch (ProdottoException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "ID o prezzo non validi.");
        }
    }

    private static void eliminaProdotto() {
        try {
            System.out.print("ID Prodotto: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Confermi eliminazione? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                boundary.eliminaProdotto(id);
                System.out.println("Prodotto eliminato con successo.");
            }
        } catch (ProdottoException e) {
            System.out.println(ERROR_PREFIX + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(ERROR_PREFIX + "ID non valido.");
        }
    }

    private static void listaProdotti() {
        List<ProdottoBean> prodotti = boundary.getProdotti();
        if (prodotti.isEmpty()) {
            System.out.println("Nessun prodotto disponibile.");
        } else {
            System.out.println("Lista Prodotti:");
            prodotti.forEach(p -> System.out.println(
                    p.getId() + " - " + p.getNome() + " (" + p.getCategoria() + ") " + p.getPrezzo() + "€"
            ));
        }
    }
}