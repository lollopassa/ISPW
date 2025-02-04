package com.biteme.app.controller;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.entity.User;
import com.biteme.app.exception.GoogleAuthException;
import com.biteme.app.service.GoogleAuthService;
import com.biteme.app.util.Configuration;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.util.HashingUtil;
import com.biteme.app.util.SceneLoader;

import java.util.regex.Pattern;

public class LoginController {

    private final UserDao userDao;
    private final GoogleAuthService googleAuthService;

    public LoginController() {
        this.userDao = Configuration.getPersistenceProvider().getDaoFactory().getUserDao();
        this.googleAuthService = new GoogleAuthService(userDao);
    }

    public boolean authenticateUser(LoginBean loginBean) {
        String emailOrUsername = loginBean.getEmailOrUsername();
        String password = loginBean.getPassword();

        if (emailOrUsername.isEmpty() || password.isEmpty()) {
            return false;
        }

        if (emailOrUsername.contains("@") && !isValidEmail(emailOrUsername)) {
            return false;
        }

        return userDao.load(emailOrUsername)
                .map(user -> validatePassword(user, password))
                .orElse(false);
    }

    private boolean validatePassword(User user, String password) {
        if (user.isGoogleUser()) {
            return false; // Utente Google non può fare login normale
        }
        return user.getPassword().equals(HashingUtil.hashPassword(password));
    }

    public User authenticateWithGoogle() throws GoogleAuthException {
        try {
            // Chiamata sicura al servizio di autenticazione Google
            return googleAuthService.authenticateWithGoogle();
        } catch (GoogleAuthException e) {
            // Puoi aggiungere un log o trattare ulteriori dettagli dell'errore qui se necessario.
            throw new GoogleAuthException("Autenticazione Google fallita. Verifica i dettagli e riprova.", e);
        }
    }
    public void navigateToHome() {
        SceneLoader.loadScene("/com/biteme/app/home.fxml", "Home - BiteMe");
    }

    public void navigateToSignup() {
        SceneLoader.loadScene("/com/biteme/app/signup.fxml", "Registrati - BiteMe");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
}
