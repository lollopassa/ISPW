package com.biteme.app.ui;

import com.biteme.app.boundary.SignupBoundary;
import com.biteme.app.controller.SignupController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupUI {

    @FXML private TextField nomeUtenteTextField;
    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordTextField;
    @FXML private PasswordField confirmPasswordTextField;

    private final SignupController signupController = new SignupController();
    private final SignupBoundary signupBoundary = new SignupBoundary(signupController);

    @FXML
    private void onRegistratiButtonClick() {
        String username        = nomeUtenteTextField.getText().trim();
        String email           = emailTextField.getText().trim();
        String password        = passwordTextField.getText();
        String confirmPassword = confirmPasswordTextField.getText();

        try {
            signupBoundary.register(username, email, password, confirmPassword);
            showAlert("Successo", "Registrazione completata!", Alert.AlertType.INFORMATION);
            signupBoundary.navigateToLogin();
        } catch (IllegalArgumentException ex) {
            showAlert("Errore di Registrazione", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onAccediButtonClick() {
        signupBoundary.navigateToLogin();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
