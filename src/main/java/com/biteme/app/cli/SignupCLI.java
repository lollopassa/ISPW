package com.biteme.app.cli;

import com.biteme.app.boundary.SignupBoundary;
import com.biteme.app.controller.SignupController;
import com.biteme.app.util.CLIUtils;

public class SignupCLI {

    private SignupCLI() {}

    private static final String RETRY_MESSAGE = "Riprova la registrazione.\n";
    private static final SignupController controller = new SignupController();
    private static final SignupBoundary boundary = new SignupBoundary(controller);

    public static void start() {
        var scanner = CLIUtils.getScanner();
        System.out.println("========== Signup ==========");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Conferma Password: ");
        String confirmPassword = scanner.nextLine();

        try {
            // CLI non costruisce più la bean
            boundary.register(username, email, password, confirmPassword);
            System.out.println("Registrazione completata con successo!");
            System.out.println("Prosegui con il login...");
            boundary.navigateToLogin();
            LoginCLI.login();
        } catch (IllegalArgumentException ex) {
            System.out.println("Errore: " + ex.getMessage());
            System.out.println(RETRY_MESSAGE);
            start();
        }
    }
}
