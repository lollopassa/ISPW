package com.biteme.app.boundary;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.controller.SignupController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupBoundary {

    @FXML
    private TextField nomeUtenteTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private PasswordField confirmPasswordTextField;

    private final SignupController signupController = new SignupController();

    @FXML
    private void onRegistratiButtonClick() {
        
        SignupBean signupBean = new SignupBean();
        signupBean.setUsername(nomeUtenteTextField.getText());
        signupBean.setEmail(emailTextField.getText());
        signupBean.setPassword(passwordTextField.getText());
        signupBean.setConfirmPassword(confirmPasswordTextField.getText());

        
        try {
            signupController.registerUser(signupBean);
            showAlert("Successo", "Registrazione completata!", Alert.AlertType.INFORMATION);
            signupController.navigateToLogin();
        } catch (IllegalArgumentException ex) {
            
            showAlert("Errore di Registrazione", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onAccediButtonClick() {
        signupController.navigateToLogin();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
