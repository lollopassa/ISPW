package com.biteme.app.bean;

import java.util.regex.Pattern;

public class LoginBean {
    private String emailOrUsername;
    private String password;

    public void validate() {
        validateNotEmpty();
        validateEmailFormatIfPresent();
    }

    private void validateNotEmpty() {
        if (emailOrUsername == null || emailOrUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Email/Username è obbligatorio.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password è obbligatoria.");
        }
    }

    private void validateEmailFormatIfPresent() {
        if (emailOrUsername != null && emailOrUsername.contains("@") && !isValidEmail()) {
            throw new IllegalArgumentException("Formato email non valido.");
        }
    }

    private boolean isValidEmail() {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,7}$";
        return Pattern.matches(emailRegex, emailOrUsername);
    }

    public String getEmailOrUsername() {
        return emailOrUsername;
    }

    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}