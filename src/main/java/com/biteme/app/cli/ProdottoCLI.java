package com.biteme.app.cli;

import java.math.BigDecimal;
import com.biteme.app.bean.ProdottoBean;
import com.biteme.app.controller.ProdottoController;
import com.biteme.app.controller.LoginController;
import com.biteme.app.model.Categoria;

public class ProdottoCLI {
    private static ProdottoController prodottoController = new ProdottoController();
    private static LoginController loginController = new LoginController();

    public static void start() {
        var scanner = CLIUtils.getScanner();
        boolean isAdmin = loginController.isUserAdmin();

        while(true) {
            System.out.println("========== Prodotto CLI ==========");

            if(isAdmin) {
                System.out.println("1. Aggiungi Prodotto");
                System.out.println("2. Modifica Prodotto");
                System.out.println("3. Elimina Prodotto");
                System.out.println("4. Lista Prodotti");
                System.out.println("5. Torna al Menu");
                System.out.print("Scegli un'opzione: ");
                String scelta = scanner.nextLine();

                if(scelta.equals("1")) {
                    System.out.print("Nome Prodotto: ");
                    String nome = scanner.nextLine();
                    System.out.print("Categoria (es. PIZZE, PRIMI, etc.): ");
                    String categoriaStr = scanner.nextLine();
                    Categoria categoria = Categoria.valueOf(categoriaStr.toUpperCase());
                    System.out.print("Prezzo: ");
                    BigDecimal prezzo = new BigDecimal(scanner.nextLine());

                    ProdottoBean bean = new ProdottoBean();
                    bean.setNome(nome);
                    bean.setCategoria(categoria);
                    bean.setPrezzo(prezzo);
                    bean.setDisponibile(true);

                    prodottoController.aggiungiProdotto(bean);
                    System.out.println("Prodotto aggiunto correttamente.");
                } else if(scelta.equals("2")) {
                    System.out.print("Inserisci l'ID del prodotto da modificare: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("Nuovo Nome: ");
                    String nome = scanner.nextLine();
                    System.out.print("Nuova Categoria (es. PIZZE, PRIMI, etc.): ");
                    Categoria categoria = Categoria.valueOf(scanner.nextLine().toUpperCase());
                    System.out.print("Nuovo Prezzo: ");
                    BigDecimal prezzo = new BigDecimal(scanner.nextLine());

                    ProdottoBean beanAggiornato = new ProdottoBean();
                    beanAggiornato.setId(id);
                    beanAggiornato.setNome(nome);
                    beanAggiornato.setCategoria(categoria);
                    beanAggiornato.setPrezzo(prezzo);
                    beanAggiornato.setDisponibile(true);

                    prodottoController.modificaProdotto(beanAggiornato);
                    System.out.println("Prodotto aggiornato.");
                } else if(scelta.equals("3")) {
                    System.out.print("Inserisci l'ID del prodotto da eliminare: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("Confermi l'eliminazione? (s/n): ");
                    String conferma = scanner.nextLine();
                    if(conferma.equalsIgnoreCase("s")) {
                        prodottoController.eliminaProdotto(id);
                        System.out.println("Prodotto eliminato.");
                    }
                } else if(scelta.equals("4")) {
                    System.out.println("Lista Prodotti:");
                    for(ProdottoBean bean : prodottoController.getProdotti()) {
                        System.out.println(bean.getId() + " - " + bean.getNome() + " - " + bean.getCategoria() + " - " + bean.getPrezzo());
                    }
                } else if(scelta.equals("5")) {
                    break;
                } else {
                    System.out.println("Opzione non valida.");
                }
            } else {
                // Se l'utente non è admin, solo visualizzazione
                System.out.println("1. Lista Prodotti");
                System.out.println("2. Torna al Menu");
                System.out.print("Scegli un'opzione: ");
                String scelta = scanner.nextLine();

                if(scelta.equals("1")) {
                    System.out.println("Lista Prodotti:");
                    for(ProdottoBean bean : prodottoController.getProdotti()) {
                        System.out.println(bean.getId() + " - " + bean.getNome() + " - " + bean.getCategoria() + " - " + bean.getPrezzo());
                    }
                } else if(scelta.equals("2")) {
                    break;
                } else {
                    System.out.println("Opzione non valida.");
                }
            }
        }
    }
}
