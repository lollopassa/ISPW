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
        // Creazione del bean con i dati forniti dall'utente
        SignupBean signupBean = new SignupBean();
        signupBean.setUsername(nomeUtenteTextField.getText());
        signupBean.setEmail(emailTextField.getText());
        signupBean.setPassword(passwordTextField.getText());
        signupBean.setConfirmPassword(confirmPasswordTextField.getText());

        // Il controller si occupa di validare e registrare l'utente.
        try {
            signupController.registerUser(signupBean);
            showAlert("Successo", "Registrazione completata!", Alert.AlertType.INFORMATION);
            signupController.navigateToLogin();
        } catch (IllegalArgumentException ex) {
            // Se c'è un errore di validazione o l'utente esiste già, viene mostrato l'errore
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
