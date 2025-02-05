package com.biteme.app.boundary;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.controller.LoginController;
import com.biteme.app.entity.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginBoundary {

    @FXML
    private TextField emailOrUsernameTextField;

    @FXML
    private PasswordField passwordTextField;

    private final LoginController loginController = new LoginController();

    @FXML
    private void onLoginButtonClick() {
        LoginBean loginBean = new LoginBean();
        loginBean.setEmailOrUsername(emailOrUsernameTextField.getText());
        loginBean.setPassword(passwordTextField.getText());

        User authenticatedUser = loginController.authenticateUserAndGetUser(loginBean);

        if (authenticatedUser != null) {
            showAlert("Successo", "Login effettuato con successo!", Alert.AlertType.INFORMATION);
            loginController.navigateToHome(authenticatedUser);
        } else {
            showAlert("Errore", "Credenziali non valide!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onGoogleLoginButtonClick() {
        try {
            User user = loginController.authenticateWithGoogle();
            showAlert("Benvenuto", "Accesso effettuato come " + user.getUsername(), Alert.AlertType.INFORMATION);
            loginController.navigateToHome(user);
        } catch (Exception e) {
            showAlert("Errore Google", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onSignupButtonClick() {
        loginController.navigateToSignup(); // Navigazione alla schermata Registrazione
    }

    @FXML
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}