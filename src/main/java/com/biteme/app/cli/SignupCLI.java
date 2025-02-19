package com.biteme.app.cli;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.controller.SignupController;
import com.biteme.app.util.CLIUtils;

public class SignupCLI {

    
    private SignupCLI() {
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

        
        SignupBean signupBean = new SignupBean();
        signupBean.setUsername(username);
        signupBean.setEmail(email);
        signupBean.setPassword(password);
        signupBean.setConfirmPassword(confirmPassword);

        SignupController signupController = new SignupController();

        try {
            
            signupController.registerUser(signupBean);
            System.out.println("Registrazione completata con successo!");
            System.out.println("Prosegui con il login...");
            LoginCLI.login();
        } catch (IllegalArgumentException ex) {
            
            System.out.println("Errore: " + ex.getMessage());
            System.out.println(RETRY_MESSAGE);
            start();
        }
    }
}
