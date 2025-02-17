package com.biteme.app.cli;

import java.util.Scanner;
import com.biteme.app.bean.LoginBean;
import com.biteme.app.controller.LoginController;
import com.biteme.app.util.CLIUtils;

public class LoginCLI {

    // Costruttore privato per evitare istanziazioni
    private LoginCLI() {
    }

    private static final Scanner scanner = CLIUtils.getScanner();

    public static void login() {
        System.out.println("========== Benvenuto ==========");
        System.out.println("1. Login");
        System.out.println("2. Registrazione");
        System.out.println("3. Login con Google");
        System.out.print("Scegli un'opzione: ");
        String scelta = scanner.nextLine();

        if (scelta.equals("1")) {
            // Procedura di Login tradizionale
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

                // Verifica se l'utente è admin
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
        } else if (scelta.equals("3")) {
            // Procedura di Login con Google
            System.out.println("========== Login con Google ==========");
            LoginController loginController = new LoginController();
            try {
                // Autenticazione tramite Google (il metodo aprirà la schermata di login di Google)
                loginController.authenticateWithGoogle();
                String username = loginController.getCurrentUsername();
                System.out.println("Login con Google effettuato con successo! Benvenuto " + username);

                // Verifica se l'utente è admin
                if (loginController.isUserAdmin()) {
                    System.out.println("Benvenuto, amministratore!");
                }

                // Avvia il menu principale
                MenuCLI.start();
            } catch (Exception ex) {
                System.out.println("Errore durante il login con Google: " + ex.getMessage());
                System.out.println("Riprova.");
                login();
            }
        } else {
            System.out.println("Opzione non valida!");
            login();
        }
    }
}
