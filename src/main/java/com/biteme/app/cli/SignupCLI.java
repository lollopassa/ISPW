package com.biteme.app.cli;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.controller.SignupController;

public class SignupCLI {

    private SignupCLI() {
        // Costruttore privato
    }

    private static final String RETRY_MESSAGE = "Riprova la registrazione.\n";

    public static void start() {
        var scanner = CLIUtils.getScanner();
        System.out.println("========== Signup ==========");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Conferma Password: ");
        String confirmPassword = scanner.nextLine();

        // Usa il costruttore vuoto e imposta i valori con i setter
        SignupBean signupBean = new SignupBean();
        signupBean.setUsername(username);
        signupBean.setEmail(email);
        signupBean.setPassword(password);
        signupBean.setConfirmPassword(confirmPassword);

        SignupController signupController = new SignupController();

        try {
            // Esegui la registrazione dell'utente
            signupController.registerUser(signupBean);
            System.out.println("Registrazione completata con successo!");
            System.out.println("Prosegui con il login...");
            LoginCLI.login();
        } catch (IllegalArgumentException ex) {
            // Mostra messaggio d'errore e riprova
            System.out.println("Errore: " + ex.getMessage());
            System.out.println(RETRY_MESSAGE);
            start();
        }
    }
}
