package com.biteme.app.view;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.controller.LoginController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginView {

    @FXML
    private TextField emailOrUsernameTextField;

    @FXML
    private PasswordField passwordTextField;

    private final LoginController loginController = new LoginController();

    @FXML
    private void onLoginButtonClick(){
    LoginBean loginBean = new LoginBean();
        loginBean.setEmailOrUsername(emailOrUsernameTextField.getText());
        loginBean.setPassword(passwordTextField.getText());

    boolean isAuthenticated = loginController.authenticateUser(loginBean);

        if (isAuthenticated) {
        showAlert("Successo", "Login effettuato con successo!", Alert.AlertType.INFORMATION);
        loginController.navigateToHome();
    } else {
        showAlert("Errore", "Credenziali non valide!", Alert.AlertType.ERROR);
    }
}


@FXML
    private void onGoogleLoginButtonClick() {
    try {
        loginController.authenticateWithGoogle();
        String username = loginController.getCurrentUsername();
        showAlert("Benvenuto", "Accesso effettuato come " + username, Alert.AlertType.INFORMATION);
        loginController.navigateToHome();
    } catch (Exception e) {
        showAlert("Errore Google", e.getMessage(), Alert.AlertType.ERROR);
    }
}

    @FXML
    private void onSignupButtonClick() {
        loginController.navigateToSignup();
    }

    @FXML
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}