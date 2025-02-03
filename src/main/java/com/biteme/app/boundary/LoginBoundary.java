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
        // Crea il bean di login con le credenziali dell'utente
        LoginBean loginBean = new LoginBean();
        loginBean.setEmailOrUsername(emailOrUsernameTextField.getText());
        loginBean.setPassword(passwordTextField.getText());

        // Chiede al controller se l'autenticazione è andata a buon fine
        if (loginController.authenticateUser(loginBean)) {
            // Autenticazione riuscita
            showAlert("Successo", "Login effettuato con successo!", Alert.AlertType.INFORMATION);
            loginController.navigateToHome(); // Naviga alla home
        } else {
            // Credenziali non valide
            showAlert("Errore", "Credenziali non valide!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onGoogleLoginButtonClick() {
        try {
            User user = loginController.authenticateWithGoogle();
            showAlert("Benvenuto", "Accesso effettuato come " + user.getUsername(), Alert.AlertType.INFORMATION);
            loginController.navigateToHome();
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