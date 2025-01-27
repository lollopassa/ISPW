package com.biteme.app.controller;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.util.Configuration;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.util.HashingUtil;
import com.biteme.app.util.SceneLoader;


import java.util.regex.Pattern;

public class LoginController {

    private final UserDao userDao;

    public LoginController() {
        this.userDao = Configuration.getPersistenceProvider().getDaoFactory().getUserDao();
    }

    public boolean authenticateUser(LoginBean loginBean) {
        String emailOrUsername = loginBean.getEmailOrUsername();
        String password = loginBean.getPassword();

        // Verifica che i campi non siano vuoti
        if (emailOrUsername.isEmpty() || password.isEmpty()) {
            return false; // Informazioni mancanti
        }

        // Valida il formato dell'email se contiene "@"
        if (emailOrUsername.contains("@") && !isValidEmail(emailOrUsername)) {
            return false; // Formato email non valido
        }

        // Recupera l'utente dal DAO
        return userDao.load(emailOrUsername).map(user -> {
            // Hash della password fornita
            String hashedPassword = HashingUtil.hashPassword(password);

            // Confronta la password hashata con quella memorizzata
            return user.getPassword().equals(hashedPassword); // True se corrispondono
        }).orElse(false); // False se l'utente non è stato trovato
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