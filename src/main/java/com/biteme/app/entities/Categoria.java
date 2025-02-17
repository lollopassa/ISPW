package com.biteme.app.entities;

public enum Categoria {
    ANTIPASTI("Antipasti"),
    PRIMI("Primi"),
    SECONDI("Secondi"),
    PIZZE("Pizze"),
    CONTORNI("Contorni"),
    BEVANDE("Bevande"),
    DOLCI("Dolci");

    private final String displayName;

    Categoria(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}