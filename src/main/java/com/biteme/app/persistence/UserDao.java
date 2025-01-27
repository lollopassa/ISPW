package com.biteme.app.persistence;

import com.biteme.app.entity.User;
import com.biteme.app.entity.UserRole;

public interface UserDao extends Dao<String, User> {
    boolean existsEmail(String email); // Nuovo metodo specifico per email
    boolean existsRole(UserRole role); // Nuovo metodo specifico per ruolo
}