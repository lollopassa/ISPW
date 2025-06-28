package com.biteme.app.controller;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.entities.User;
import com.biteme.app.entities.UserRole;
import com.biteme.app.exception.GoogleAuthException;
import com.biteme.app.googleapi.GoogleAuthService;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.googleapi.GoogleAuthUtility;
import com.biteme.app.util.HashingUtil;
import com.biteme.app.util.SceneLoader;
import com.biteme.app.util.UserSession;
import com.biteme.app.persistence.UserDao;

import java.util.Optional;

public class LoginController {

    private final UserDao userDao;
    private final GoogleAuthService googleAuthService;

    public LoginController() {
        this.userDao = Configuration.getPersistenceProvider().getDaoFactory().getUserDao();
        this.googleAuthService = new GoogleAuthService();
    }

    public void authenticateUser(LoginBean loginBean) {
                Optional<User> optionalUser = userDao.read(loginBean.getEmailOrUsername());
        if (!optionalUser.isPresent()) {
            throw new IllegalArgumentException("Utente non trovato.");
        }
        User user = optionalUser.get();

                if (user.isGoogleUser()) {
            throw new IllegalArgumentException("Utente Google non può autenticarsi tramite metodo tradizionale.");
        }

                if (!user.getPassword().equals(HashingUtil.hashPassword(loginBean.getPassword()))) {
            throw new IllegalArgumentException("Password errata.");
        }

                UserSession.setCurrentUser(user);
    }

    public void authenticateWithGoogle() throws GoogleAuthException {
        try {
            String accessToken = googleAuthService.authenticateWithGoogle();
            GoogleAuthUtility.GoogleUserData googleUser = googleAuthService.getGoogleUserData(accessToken);

            User user = userDao.read(googleUser.getEmail())
                    .map(this::validateGoogleUser)
                    .orElseGet(() -> {
                        User newUser = googleAuthService.createGoogleUser(googleUser);
                        userDao.create(newUser);
                        return newUser;
                    });

            UserSession.setCurrentUser(user);
        } catch (GoogleAuthException e) {
            throw new GoogleAuthException("Autenticazione Google fallita. Verifica i dettagli e riprova.", e);
        }
    }

    private User validateGoogleUser(User user) {
        if (!user.isGoogleUser()) {
            throw new IllegalStateException("Email già registrata con metodo tradizionale");
        }
        return user;
    }

    public void navigateToHome() {
        User user = UserSession.getCurrentUser();
        String scenePath = user.getRuolo() == UserRole.ADMIN
                ? "/com/biteme/app/adminHome.fxml"
                : "/com/biteme/app/home.fxml";
        String title = user.getRuolo() == UserRole.ADMIN
                ? "Admin Home - BiteMe"
                : "Home - BiteMe";
        SceneLoader.getInstance().loadScene(scenePath, title);
    }

        String getHomeScenePath() {
        User user = UserSession.getCurrentUser();
        return user.getRuolo() == UserRole.ADMIN
                ? "/com/biteme/app/adminHome.fxml"
                : "/com/biteme/app/home.fxml";
    }

    public void navigateToSignup() {
        SceneLoader.getInstance().loadScene("/com/biteme/app/signup.fxml", "Registrati - BiteMe");
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