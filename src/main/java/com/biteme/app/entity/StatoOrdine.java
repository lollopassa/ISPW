package com.biteme.app.entity;

public enum StatoOrdine {
    NUOVO("Nuovo"),
    IN_CORSO("In corso"),
    COMPLETATO("Completato");


    private final String displayName;

    StatoOrdine(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
