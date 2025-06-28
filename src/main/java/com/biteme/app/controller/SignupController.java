package com.biteme.app.controller;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.entities.User;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.util.SceneLoader;

public class SignupController {

    private final UserDao userDao;

    public SignupController() {
        this.userDao = Configuration.getPersistenceProvider().getDaoFactory().getUserDao();
    }

    public void registerUser(SignupBean signupBean) {
        signupBean.validate(); // <-- ora è la bean a fare la validazione

        if (userDao.read(signupBean.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email già registrata");
        }

        if (userDao.read(signupBean.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username già registrato");
        }

        User user = new User(signupBean.getUsername());
        user.setEmail(signupBean.getEmail());
        user.setPassword(signupBean.getPassword());
        userDao.create(user);
    }

        public void navigateToLogin() {
        SceneLoader.getInstance().loadScene("/com/biteme/app/login.fxml", "Login - BiteMe");
    }
}
