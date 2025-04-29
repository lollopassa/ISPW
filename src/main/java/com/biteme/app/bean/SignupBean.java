package com.biteme.app.bean;

public class SignupBean {

    private String email;
    private String username;
    private String password;
    private String confirmPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void validate() {
        if (email == null ||  email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,7}$")) {
            throw new IllegalArgumentException("Email non valida");
        }

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username mancante");
        }

        if (password == null || password.isEmpty() || confirmPassword == null  || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException("Le password sono obbligatorie");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Le password non corrispondono");
        }
    }
}
