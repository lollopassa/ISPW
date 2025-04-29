package com.biteme.app.boundary;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.controller.LoginController;
import com.biteme.app.exception.GoogleAuthException;

public class LoginBoundary {

    private final LoginController loginController;


    public LoginBoundary(LoginController loginController) {
        this.loginController = loginController;
    }


    public void login(LoginBean loginBean) {
        loginBean.validate();
        loginController.authenticateUser(loginBean);
    }


    public void loginWithGoogle() throws GoogleAuthException {
        loginController.authenticateWithGoogle();
    }
}
