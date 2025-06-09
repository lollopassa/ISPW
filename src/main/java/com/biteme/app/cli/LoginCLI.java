package com.biteme.app.cli;

import java.util.Scanner;
import com.biteme.app.boundary.LoginBoundary;
import com.biteme.app.controller.LoginController;
import com.biteme.app.exception.GoogleAuthException;
import com.biteme.app.util.CLIUtils;

public class LoginCLI {

    private LoginCLI() {}

    private static final Scanner scanner = CLIUtils.getScanner();
    private static final LoginController controller = new LoginController();
    private static final LoginBoundary boundary = new LoginBoundary(controller);

    public static void login() {
        System.out.println("========== Benvenuto ==========");
        System.out.println("1. Login");
        System.out.println("2. Registrazione");
        System.out.println("3. Login con Google");
        System.out.print("Scegli un'opzione: ");
        String scelta = scanner.nextLine();

        if (scelta.equals("1")) {
            System.out.println("========== Login CLI ==========");
            System.out.print("Inserisci Email o Username: ");
            String emailOrUsername = scanner.nextLine();
            System.out.print("Inserisci Password: ");
            String password = scanner.nextLine();

            try {
                boundary.login(emailOrUsername, password);
                System.out.println("Login effettuato con successo! Benvenuto " + emailOrUsername);
                if (controller.isUserAdmin()) {
                    System.out.println("Benvenuto, amministratore!");
                }
                MenuCLI.start();
            } catch (IllegalArgumentException ex) {
                System.out.println("Errore: " + ex.getMessage());
                System.out.println("Credenziali non valide! Riprova.");
                login();
            }
        } else if (scelta.equals("2")) {
            SignupCLI.start();
        } else if (scelta.equals("3")) {
            System.out.println("========== Login con Google ==========");
            try {
                boundary.loginWithGoogle();
                String username = controller.getCurrentUsername();
                System.out.println("Login con Google effettuato con successo! Benvenuto " + username);
                if (controller.isUserAdmin()) {
                    System.out.println("Benvenuto, amministratore!");
                }
                MenuCLI.start();
            } catch (GoogleAuthException ex) {
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
