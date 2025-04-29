package com.biteme.app.ui;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.boundary.SignupBoundary;
import com.biteme.app.controller.SignupController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * UI Controller FXML: gestisce eventi UI e delega al Boundary.
 */
public class SignupUI {

    @FXML private TextField nomeUtenteTextField;
    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordTextField;
    @FXML private PasswordField confirmPasswordTextField;

    private final SignupController signupController = new SignupController();
    private final SignupBoundary signupBoundary = new SignupBoundary(signupController);

    @FXML
    private void onRegistratiButtonClick() {
        SignupBean signupBean = new SignupBean();
        signupBean.setUsername(nomeUtenteTextField.getText());
        signupBean.setEmail(emailTextField.getText());
        signupBean.setPassword(passwordTextField.getText());
        signupBean.setConfirmPassword(confirmPasswordTextField.getText());

        try {
            signupBoundary.register(signupBean);
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