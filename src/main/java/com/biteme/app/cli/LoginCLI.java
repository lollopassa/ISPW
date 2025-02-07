package com.biteme.app.cli;

import java.util.Scanner;
import com.biteme.app.bean.LoginBean;
import com.biteme.app.controller.LoginController;

public class LoginCLI {

    private LoginCLI() {
        //costruttore privato
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static void login() {
        System.out.println("========== Benvenuto ==========");
        System.out.println("1. Login");
        System.out.println("2. Registrazione");
        System.out.print("Scegli un'opzione: ");
        String scelta = scanner.nextLine();

        if (scelta.equals("1")) {
            // Procedura di Login
            System.out.println("========== Login CLI ==========");
            System.out.print("Inserisci Email o Username: ");
            String emailOrUsername = scanner.nextLine();
            System.out.print("Inserisci Password: ");
            String password = scanner.nextLine();

            // Crea il bean con le credenziali dell'utente
            LoginBean loginBean = new LoginBean();
            loginBean.setEmailOrUsername(emailOrUsername);
            loginBean.setPassword(password);

            // Istanzia il controller
            LoginController loginController = new LoginController();

            try {
                // Esegui l'autenticazione utente
                loginController.authenticateUser(loginBean);

                // Se l'autenticazione ha successo
                System.out.println("Login effettuato con successo! Benvenuto " + emailOrUsername);

                // Verifica se l'utente Ã¨ admin
                if (loginController.isUserAdmin()) {
                    System.out.println("Benvenuto, amministratore!");
                }

                // Avvia il menu principale
                MenuCLI.start();
            } catch (IllegalArgumentException ex) {
                // L'autenticazione fallisce se viene sollevata un'eccezione
                System.out.println("Errore: " + ex.getMessage());
                System.out.println("Credenziali non valide! Riprova.");
                login(); // Riprova il login
            }
        } else if (scelta.equals("2")) {
            // Procedura di Registrazione
            SignupCLI.start();
        } else {
            System.out.println("Opzione non valida!");
            login();
        }
    }
}
