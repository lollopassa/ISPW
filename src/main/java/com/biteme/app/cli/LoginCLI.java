package com.biteme.app.cli;

import java.util.Scanner;
import com.biteme.app.bean.LoginBean;
import com.biteme.app.controller.LoginController;

public class LoginCLI {
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

            LoginBean loginBean = new LoginBean();
            loginBean.setEmailOrUsername(emailOrUsername);
            loginBean.setPassword(password);

            LoginController loginController = new LoginController();
            boolean authenticatedUser = loginController.authenticateUser(loginBean);

            if (authenticatedUser) {
                System.out.println("Login effettuato con successo! Benvenuto " + emailOrUsername);

                // Verifica se l'utente è admin
                if (loginController.isUserAdmin()) {
                    System.out.println("Benvenuto, amministratore!");
                }

                MenuCLI.start(); // Avvia il menu principale
            } else {
                System.out.println("Credenziali non valide! Riprova.");
                // Riprova il login (oppure potresti aggiungere un'opzione per uscire)
                login();
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
