package com.biteme.app.entity;

public enum UserRole {
    ADMIN("Admin"),
    CAMERIERE("Cameriere");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static UserRole fromString(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.displayName.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Ruolo non valido: " + value);
    }
}