package com.biteme.app.controller;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.model.User;
import com.biteme.app.model.UserRole;
import com.biteme.app.exception.GoogleAuthException;
import com.biteme.app.service.GoogleAuthService;
import com.biteme.app.util.Configuration;
import com.biteme.app.util.GoogleAuthUtility;
import com.biteme.app.util.HashingUtil;
import com.biteme.app.util.SceneLoader;
import com.biteme.app.util.UserSession;
import com.biteme.app.persistence.UserDao;

import java.util.Optional;
import java.util.regex.Pattern;

public class LoginController {

    private final UserDao userDao;
    private final GoogleAuthService googleAuthService;

    public LoginController() {
        this.userDao = Configuration.getPersistenceProvider().getDaoFactory().getUserDao();
        this.googleAuthService = new GoogleAuthService();
    }

    public void authenticateUser(LoginBean loginBean) {
        // Controllo che i campi non siano vuoti
        if (loginBean.getEmailOrUsername() == null || loginBean.getEmailOrUsername().trim().isEmpty() ||
                loginBean.getPassword() == null || loginBean.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Email/Username e password sono obbligatori.");
        }

        // Se si tratta di un'email, controlla il formato
        if (loginBean.getEmailOrUsername().contains("@") && !isValidEmail(loginBean.getEmailOrUsername())) {
            throw new IllegalArgumentException("Il formato dell'email non è valido.");
        }

        // Prova a caricare l'utente dal DAO
        Optional<User> optionalUser = userDao.load(loginBean.getEmailOrUsername());
        if (!optionalUser.isPresent()) {
            throw new IllegalArgumentException("Utente non trovato.");
        }
        User user = optionalUser.get();

        // Se l'utente è stato creato tramite Google, non può autenticarsi con password tradizionale
        if (user.isGoogleUser()) {
            throw new IllegalArgumentException("Utente Google non può autenticarsi tramite metodo tradizionale.");
        }

        // Verifica la password (hashata) corrisponda a quella salvata
        if (!user.getPassword().equals(HashingUtil.hashPassword(loginBean.getPassword()))) {
            throw new IllegalArgumentException("Password errata.");
        }

        // Se tutto è valido, imposta l'utente corrente
        UserSession.setCurrentUser(user);
    }

    /**
     * Autentica un utente tramite Google.
     * Se l'autenticazione fallisce, viene lanciata un'eccezione.
     *
     * @throws GoogleAuthException se l'autenticazione Google fallisce
     */
    public void authenticateWithGoogle() throws GoogleAuthException {
        try {
            String accessToken = googleAuthService.authenticateWithGoogle();
            GoogleAuthUtility.GoogleUserData googleUser = googleAuthService.getGoogleUserData(accessToken);

            User user = userDao.load(googleUser.getEmail())
                    .map(this::validateGoogleUser)
                    .orElseGet(() -> {
                        User newUser = googleAuthService.createGoogleUser(googleUser);
                        userDao.store(newUser);
                        return newUser;
                    });

            UserSession.setCurrentUser(user);
        } catch (GoogleAuthException e) {
            throw new GoogleAuthException("Autenticazione Google fallita. Verifica i dettagli e riprova.", e);
        }
    }

    /**
     * Se l'utente esiste ma non è un utente Google, questo metodo lancia un'eccezione.
     */
    private User validateGoogleUser(User user) {
        if (!user.isGoogleUser()) {
            throw new IllegalStateException("Email già registrata con metodo tradizionale");
        }
        return user;
    }

    /**
     * Carica la schermata appropriata in base al ruolo dell'utente.
     */
    public void navigateToHome() {
        User user = UserSession.getCurrentUser();

        if (user.getRuolo() == UserRole.ADMIN) {
            SceneLoader.loadScene("/com/biteme/app/adminHome.fxml", "Admin Home - BiteMe");
        } else {
            SceneLoader.loadScene("/com/biteme/app/home.fxml", "Home - BiteMe");
        }
    }

    public void navigateToSignup() {
        SceneLoader.loadScene("/com/biteme/app/signup.fxml", "Registrati - BiteMe");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    public String getCurrentUsername() {
        User currentUser = UserSession.getCurrentUser();
        return currentUser != null ? currentUser.getUsername() : "";
    }

    public boolean isUserAdmin() {
        User currentUser = UserSession.getCurrentUser();
        return currentUser != null && currentUser.getRuolo() == UserRole.ADMIN;
    }

    public void logout() {
        UserSession.clear();
    }
}