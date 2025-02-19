package com.biteme.app.persistence;

import com.biteme.app.entities.User;

public interface UserDao extends Dao<String, User> {
    boolean existsEmail(String email); }