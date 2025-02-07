package com.biteme.app.controller;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.model.User;
import com.biteme.app.model.UserRole;
import com.biteme.app.exception.GoogleAuthException;
import com.biteme.app.service.GoogleAuthService;
import com.biteme.app.util.*;
import com.biteme.app.persistence.UserDao;


import java.util.regex.Pattern;

public class LoginController {

    private final UserDao userDao;
    private final GoogleAuthService googleAuthService;

    public LoginController() {
        this.userDao = Configuration.getPersistenceProvider().getDaoFactory().getUserDao();
        this.googleAuthService = new GoogleAuthService();
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

        User user = userDao.load(emailOrUsername)
                .filter(u -> validatePassword(u, password))
                .orElse(null);

        if (user != null) {
            UserSession.setCurrentUser(user);
            return true;
        }
        return false;
    }

    private boolean validatePassword(User user, String password) {
        if (user.isGoogleUser()) {
            return false;
        }
        return user.getPassword().equals(HashingUtil.hashPassword(password));
    }

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

    private User validateGoogleUser(User user) {
        if (!user.isGoogleUser()) {
            throw new IllegalStateException("Email già registrata con metodo tradizionale");
        }
        return user;
    }

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
    public boolean isUserAdmin(){
        User currentUser = UserSession.getCurrentUser();
        return currentUser != null && currentUser.getRuolo() == UserRole.ADMIN;
    }
    public void logout() {
        UserSession.clear();
    }
}
