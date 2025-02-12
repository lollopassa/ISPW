package com.biteme.app.persistence;

import com.biteme.app.model.User;

public interface UserDao extends Dao<String, User> {
    boolean existsEmail(String email); // Nuovo metodo specifico per email
}