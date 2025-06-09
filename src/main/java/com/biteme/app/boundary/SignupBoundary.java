package com.biteme.app.boundary;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.controller.SignupController;


public class SignupBoundary {

    private final SignupController signupController;


    public SignupBoundary(SignupController signupController) {
        this.signupController = signupController;
    }


    public void register(String username,
                         String email,
                         String password,
                         String confirmPassword) {
        SignupBean bean = new SignupBean();
        bean.setUsername(username);
        bean.setEmail(email);
        bean.setPassword(password);
        bean.setConfirmPassword(confirmPassword);
        bean.validate();
        signupController.registerUser(bean);
    }


    public void navigateToLogin() {
        signupController.navigateToLogin();
    }
}