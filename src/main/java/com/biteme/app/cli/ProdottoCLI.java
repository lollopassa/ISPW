package com.biteme.app.cli;

import java.math.BigDecimal;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.LoginController;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.exception.ProdottoException;
import com.biteme.app.util.CLIUtils;

public class ProdottoCLI {

    // Costruttore privato per evitare istanziazioni
    private ProdottoCLI() {
    }

    private static final ProdottoController prodottoController = new ProdottoController();
    private static final LoginController loginController = new LoginController();
    private static final String ERROR_MESSAGE = "errore";

    public static void start() {
        var scanner = CLIUtils.getScanner();
        boolean isAdmin = loginController.isUserAdmin();
        boolean running = true;

        while (running) {
            System.out.println("========== Prodotto CLI ==========");

            if (isAdmin) {
                running = handleAdminOptions(scanner);
            } else {
                running = handleUserOptions(scanner);
            }
        }
    }

    private static boolean handleAdminOptions(java.util.Scanner scanner) {
        System.out.println("1. Aggiungi Prodotto");
        System.out.println("2. Modifica Prodotto");
        System.out.println("3. Elimina Prodotto");
        System.out.println("4. Lista Prodotti");
        System.out.println("5. Torna al Menu");
        System.out.print("Scegli un'opzione: ");
        String scelta = scanner.nextLine();

        switch (scelta) {
            case "1" -> aggiungiProdotto(scanner);
            case "2" -> modificaProdotto(scanner);
            case "3" -> eliminaProdotto(scanner);
            case "4" -> listaProdotti();
            case "5" -> {
                return false;
            }
            default -> System.out.println("Opzione non valida.");
        }
        return true;
    }

    private static boolean handleUserOptions(java.util.Scanner scanner) {
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

    private static void aggiungiProdotto(java.util.Scanner scanner) {
        try {
            System.out.print("Nome Prodotto: ");
            String nome = scanner.nextLine();
            System.out.print("Categoria (PIZZE, PRIMI, ANTIPASTI, BEVANDE, CONTORNI, DOLCI): ");
            String categoria = scanner.nextLine(); // Inserito dall'utente come stringa
            System.out.print("Prezzo: ");
            BigDecimal prezzo = new BigDecimal(scanner.nextLine()); // Gestione numerica

            ProdottoBean bean = new ProdottoBean();
            bean.setNome(nome);
            bean.setCategoria(categoria.toUpperCase());
            bean.setPrezzo(prezzo);
            bean.setDisponibile(true);

            prodottoController.aggiungiProdotto(bean);
            System.out.println("Prodotto aggiunto correttamente.");
        } catch (ProdottoException e) {
            // Stampe di errore sul terminale
            System.out.println(ERROR_MESSAGE + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Errore: Il prezzo deve essere un valore numerico.");
        }
    }

    private static void modificaProdotto(java.util.Scanner scanner) {
        try {
            System.out.print("Inserisci l'ID del prodotto da modificare: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Nuovo Nome: ");
            String nome = scanner.nextLine();
            System.out.print("Nuova Categoria (PIZZE, PRIMI, ANTIPASTI, BEVANDE, CONTORNI, DOLCI): ");
            String categoria = scanner.nextLine();
            System.out.print("Nuovo Prezzo: ");
            BigDecimal prezzo = new BigDecimal(scanner.nextLine());

            ProdottoBean beanAggiornato = new ProdottoBean();
            beanAggiornato.setId(id);
            beanAggiornato.setNome(nome);
            beanAggiornato.setCategoria(categoria.toUpperCase());
            beanAggiornato.setPrezzo(prezzo);
            beanAggiornato.setDisponibile(true);

            prodottoController.modificaProdotto(beanAggiornato);
            System.out.println("Prodotto aggiornato con successo.");
        } catch (ProdottoException e) {
            System.out.println("Errore: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Errore: Il prezzo deve essere un valore numerico.");
        }
    }

    private static void eliminaProdotto(java.util.Scanner scanner) {
        try {
            System.out.print("Inserisci l'ID del prodotto da eliminare: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Confermi l'eliminazione? (s/n): ");
            String conferma = scanner.nextLine();

            if (conferma.equalsIgnoreCase("s")) {
                prodottoController.eliminaProdotto(id);
                System.out.println("Prodotto eliminato con successo.");
            }
        } catch (ProdottoException e) {
            System.out.println("Errore: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Errore: L'ID del prodotto deve essere un valore numerico.");
        }
    }

    private static void listaProdotti() {
        System.out.println("Lista Prodotti:");
        for (ProdottoBean bean : prodottoController.getProdotti()) {
            System.out.println(bean.getId() + " - " + bean.getNome() + " - " + bean.getCategoria() + " - " + bean.getPrezzo());
        }
    }
}
