package com.biteme.app.cli;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.controller.SignupController;

public class SignupCLI {

    private SignupCLI() {
        //costruttore privato
    }

    private static final String RETRY_MESSAGE = "Riprova la registrazione.\n";
    public static void start() {
        var scanner = CLIUtils.getScanner();
        System.out.println("========== Signup CLI ==========");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Conferma Password: ");
        String confirmPassword = scanner.nextLine();

        SignupBean signupBean = new SignupBean();
        signupBean.setUsername(username);
        signupBean.setEmail(email);
        signupBean.setPassword(password);
        signupBean.setConfirmPassword(confirmPassword);

        SignupController signupController = new SignupController();
        if (!signupController.isValid(signupBean)) {
            System.out.println("Errore di Validazione: " + signupController.getErrorMessage());
            System.out.println(RETRY_MESSAGE);
            start();
        } else {
            try {
                if (signupController.registerUser(signupBean)) {
                    System.out.println("Registrazione completata con successo!");
                    LoginCLI.login();
                } else {
                    System.out.println("Errore: " + signupController.getErrorMessage());
                    System.out.println(RETRY_MESSAGE);
                    start();
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Errore di Registrazione: " + ex.getMessage());
                System.out.println(RETRY_MESSAGE);
                start();
            }
        }
    }
}