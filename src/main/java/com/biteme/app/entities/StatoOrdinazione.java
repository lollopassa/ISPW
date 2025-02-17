package com.biteme.app.entities;

public enum StatoOrdinazione {
    NUOVO("Nuovo"),
    IN_CORSO("In corso"),
    COMPLETATO("Completato");


    private final String displayName;

    StatoOrdinazione(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
