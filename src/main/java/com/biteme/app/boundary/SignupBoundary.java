package com.biteme.app.boundary;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.controller.SignupController;


public class SignupBoundary {

    private final SignupController signupController;


    public SignupBoundary(SignupController signupController) {
        this.signupController = signupController;
    }


    public void register(SignupBean bean) {
        bean.validate();
        signupController.registerUser(bean);
    }


    public void navigateToLogin() {
        signupController.navigateToLogin();
    }
}