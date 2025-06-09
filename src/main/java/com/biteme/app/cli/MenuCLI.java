package com.biteme.app.cli;

import com.biteme.app.controller.LoginController;
import com.biteme.app.exception.OrdineException;
import com.biteme.app.util.CLIUtils;

public class MenuCLI {

    
    private MenuCLI() {
    }

    public static void start() {

        LoginController loginController = new LoginController();

        var scanner = CLIUtils.getScanner();

        while (true) {
            System.out.println("========== Menu CLI ==========");
            System.out.println("1. Home");
            System.out.println("2. Prenotazioni");
            System.out.println("3. Ordinazioni");
            System.out.println("4. Magazzino (Prodotti)");
            System.out.println("5. Logout (torna a Login)");
            System.out.println("6. Esci");
            System.out.print("Scegli un'opzione: ");
            String scelta = scanner.nextLine();

            
            boolean isAdmin = loginController.isUserAdmin();

            switch (scelta) {
                case "1":
                    
                    if (isAdmin) {
                        AdminHomeCLI.start();
                    } else {
                        HomeCLI.start();
                    }
                    break;
                case "2":
                    PrenotazioneCLI.start();
                    break;
                case "3":
                    try {
                        OrdinazioneCLI.start();
                    } catch (OrdineException _) {
                        System.out.println("Errore.");
                    }
                    break;
                case "4":
                    
                    if (isAdmin) {
                        ProdottoCLI.start();
                    } else {
                        System.out.println("Accesso negato. Solo gli amministratori possono gestire i prodotti.");
                    }
                    break;
                case "5":
                    
                    loginController.logout();
                    System.out.println("Logout effettuato. Arrivederci!");
                    LoginCLI.login();
                    return;
                case "6":
                    System.out.println("Chiusura dell'applicazione.");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opzione non valida.");
            }
        }
    }
}
