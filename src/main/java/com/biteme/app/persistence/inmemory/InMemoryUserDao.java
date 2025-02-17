package com.biteme.app.persistence.inmemory;

import com.biteme.app.entities.User;
import com.biteme.app.entities.UserRole;
import com.biteme.app.exception.SignupException;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.util.HashingUtil;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserDao implements UserDao {

    private final Map<String, User> users = Storage.getInstance().getUsers();

    @Override
    public Optional<User> load(String identifier) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(identifier) || u.getEmail().equals(identifier))
                .findFirst();
    }

    @Override
    public void store(User user) {
        if (existsEmail(user.getEmail())) {
            throw new SignupException("Email già registrata");
        }

        if (users.isEmpty()) {
            user.setRuolo(UserRole.ADMIN);
        } else if (user.getRuolo() == null) {
            user.setRuolo(UserRole.CAMERIERE);
        }

        // Gestione utente Google
        if (!user.isGoogleUser()) {
            String hashedPassword = HashingUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
        }

        users.put(user.getUsername(), user); // Usa ancora username come chiave
    }

    @Override
    public void delete(String key) {
        users.remove(key); // Rimuove per username
    }

    @Override
    public boolean exists(String identifier) {
        return users.values().stream()
                .anyMatch(u -> u.getUsername().equals(identifier) || u.getEmail().equals(identifier));
    }

    @Override
    public boolean existsEmail(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equals(email));
    }

}