package com.biteme.app.entity;

public enum TipoOrdine {
    AL_TAVOLO("Al Tavolo"),
    ASPORTO("Asporto");

    private final String displayName;

    TipoOrdine(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}