package com.biteme.app.controller;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.util.Configuration;
import com.biteme.app.entity.User;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.util.SceneLoader;

public class SignupController {

    private final UserDao userDao;
    private String errorMessage; // Per memorizzare eventuali messaggi di errore

    public SignupController() {
        this.userDao = Configuration.getPersistenceProvider().getDaoFactory().getUserDao();
    }

    // Metodo per validare i dati di registrazione
    public boolean isValid(SignupBean signupBean) {
        String email = signupBean.getEmail();
        String username = signupBean.getUsername();
        String password = signupBean.getPassword();
        String confirmPassword = signupBean.getConfirmPassword();

        if (email == null || email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,7}$")) {
            errorMessage = "Email non valida";
            return false;
        }

        if (username == null || username.isEmpty()) {
            errorMessage = "Username mancante";
            return false;
        }

        if (password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            errorMessage = "Le password sono obbligatorie";
            return false;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage = "Le password non corrispondono";
            return false;
        }

        // Verifica se l'utente o l'email esiste già nel database
        if (userDao.load(email).isPresent()) {
            errorMessage = "Email già registrata";
            return false;
        }

        if (userDao.load(username).isPresent()) {
            errorMessage = "Username già registrato";
            return false;
        }

        return true; // Tutti i controlli sono superati
    }

    // Metodo per registrare un utente
    public boolean registerUser(SignupBean signupBean) {
        if (!isValid(signupBean)) { // Usa il nuovo metodo isValid
            return false;
        }

        if (userDao.exists(signupBean.getUsername())) {
            return false; // L'utente esiste già
        }

        User user = new User(signupBean.getUsername());
        user.setEmail(signupBean.getEmail());
        user.setPassword(signupBean.getPassword());
        userDao.store(user);

        return true;
    }

    // Metodo per navigare al login
    public void navigateToLogin() {
        SceneLoader.loadScene("/com/biteme/app/login.fxml", "Login - BiteMe");
    }

    // Metodo per ottenere il messaggio di errore
    public String getErrorMessage() {
        return errorMessage;
    }
}