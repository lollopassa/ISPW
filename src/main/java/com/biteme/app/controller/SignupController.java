package com.biteme.app.controller;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.model.User;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.util.SceneLoader;

public class SignupController {

    private final UserDao userDao;

    public SignupController() {
        this.userDao = Configuration.getPersistenceProvider().getDaoFactory().getUserDao();
    }

    // Metodo per registrare un utente con validazione interna
    public void registerUser(SignupBean signupBean) {
        // Esegui la validazione; se fallisce, lancia un'eccezione
        validate(signupBean);

        // Controllo aggiuntivo: se l'utente o l'email esistono già
        if (userDao.load(signupBean.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email già registrata");
        }
        if (userDao.load(signupBean.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username già registrato");
        }

        // Se tutti i controlli sono superati, crea e registra l'utente
        User user = new User(signupBean.getUsername());
        user.setEmail(signupBean.getEmail());
        user.setPassword(signupBean.getPassword());
        userDao.store(user);
    }

    // Metodo di validazione: se qualcosa non va, lancia un'eccezione
    private void validate(SignupBean signupBean) {
        String email = signupBean.getEmail();
        String username = signupBean.getUsername();
        String password = signupBean.getPassword();
        String confirmPassword = signupBean.getConfirmPassword();

        if (email == null || email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,7}$")) {
            throw new IllegalArgumentException("Email non valida");
        }

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username mancante");
        }

        if (password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException("Le password sono obbligatorie");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Le password non corrispondono");
        }
    }

    // Metodo per navigare al login
    public void navigateToLogin() {
        SceneLoader.loadScene("/com/biteme/app/login.fxml", "Login - BiteMe");
    }
}
