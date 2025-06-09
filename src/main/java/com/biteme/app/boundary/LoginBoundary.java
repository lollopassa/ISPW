package com.biteme.app.boundary;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.controller.LoginController;
import com.biteme.app.exception.GoogleAuthException;

public class LoginBoundary {

    private final LoginController loginController;


    public LoginBoundary(LoginController loginController) {
        this.loginController = loginController;
    }


    public void login(String emailOrUsername, String password) {
        LoginBean loginBean = new LoginBean();
        loginBean.setEmailOrUsername(emailOrUsername);
        loginBean.setPassword(password);

        loginBean.validate();
        loginController.authenticateUser(loginBean);
    }


    public void loginWithGoogle() throws GoogleAuthException {
        loginController.authenticateWithGoogle();
    }
}
