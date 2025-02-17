package com.biteme.app.entities;

public enum TipoOrdinazione {
    AL_TAVOLO("Al Tavolo"),
    ASPORTO("Asporto");

    private final String displayName;

    TipoOrdinazione(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}