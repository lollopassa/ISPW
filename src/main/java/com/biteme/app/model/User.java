package com.biteme.app.model;

public class User {

    private String username;
    private String email;
    private String password;
    private UserRole ruolo;
    private boolean googleUser;

    // Costruttori
    public User(String username) {
        this.username = username;
    }

    public User(String username, String email, String password, UserRole ruolo) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.ruolo = ruolo;
    }

    // Getter e Setter per username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter e Setter per email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter e Setter per password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getter e Setter per ruolo
    public UserRole getRuolo() {
        return ruolo;
    }

    public void setRuolo(UserRole ruolo) {
        this.ruolo = ruolo;
    }

    public boolean isGoogleUser() {
        return googleUser;
    }

    public void setGoogleUser(boolean googleUser) {
        this.googleUser = googleUser;
    }

}