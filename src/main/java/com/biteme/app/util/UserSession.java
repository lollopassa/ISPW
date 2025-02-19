package com.biteme.app.util;

import com.biteme.app.entities.User;

public class UserSession {
    private static User currentUser;

    private UserSession() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}