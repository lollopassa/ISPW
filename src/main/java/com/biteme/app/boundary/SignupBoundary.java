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

        // Validazione iniziale tramite il bean
        if (!signupController.isValid(signupBean)) { // Passaggio corretto del bean come argomento
            showAlert("Errore di Validazione", signupController.getErrorMessage(), Alert.AlertType.ERROR);
            return;
        }

        // Chiamata al controller per registrare l'utente
        try {
            if (signupController.registerUser(signupBean)) {
                showAlert("Successo", "Registrazione completata!", Alert.AlertType.INFORMATION);
                signupController.navigateToLogin();
            } else {
                showAlert("Errore", signupController.getErrorMessage(), Alert.AlertType.ERROR);
            }
        } catch (IllegalArgumentException ex) {
            // Eccezione specifica per utente già registrato o altri errori da DB
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