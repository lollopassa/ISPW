package com.biteme.app.ui;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.boundary.LoginBoundary;
import com.biteme.app.controller.LoginController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class LoginUI {

    @FXML private TextField emailOrUsernameTextField;
    @FXML private PasswordField passwordTextField;

    private final LoginController loginController = new LoginController();
    private final LoginBoundary loginBoundary = new LoginBoundary(loginController);

    @FXML
    private void onLoginButtonClick() {
        String emailOrUsername = emailOrUsernameTextField.getText();
        String password = passwordTextField.getText();

        try {
            loginBoundary.login(emailOrUsername, password);
            showAlert("Successo", "Login effettuato con successo!", AlertType.INFORMATION);
            loginController.navigateToHome();
        } catch (IllegalArgumentException ex) {
            showAlert("Errore", ex.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void onGoogleLoginButtonClick() {
        try {
            loginBoundary.loginWithGoogle();
            String username = loginController.getCurrentUsername();
            showAlert("Benvenuto", "Accesso effettuato come " + username, AlertType.INFORMATION);
            loginController.navigateToHome();
        } catch (Exception e) {
            showAlert("Errore Google", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void onSignupButtonClick() {
        loginController.navigateToSignup();
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}