package com.biteme.app.persistence.txt;

import com.biteme.app.entities.User;
import com.biteme.app.entities.UserRole;
import com.biteme.app.exception.SignupException;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.util.HashingUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TxtUserDao implements UserDao {
    private static final Logger LOGGER = Logger.getLogger(TxtUserDao.class.getName());
    private final List<User> users;
    private static final String FILE_PATH = "data/users.txt";
    private static final String DELIMITER = "\\|";
    private static final String DELIMITER_OUT = "|";

    public TxtUserDao() {
        users = loadFromFile();
    }

    private List<User> loadFromFile() {
        List<User> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                User u = deserialize(line);
                if (u != null) list.add(u);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento degli utenti", e);
        }
        return list;
    }

    private void saveToFile() {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nella creazione della directory", e);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User u : users) {
                bw.write(serialize(u));
                bw.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio degli utenti", e);
        }
    }

    @Override
    public Optional<User> load(String identifier) {
        return users.stream()
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
        if (!user.isGoogleUser()) {
            String hashedPassword = HashingUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
        }
        users.add(user);
        saveToFile();
    }

    @Override
    public void delete(String key) {
        users.removeIf(u -> u.getUsername().equals(key));
        saveToFile();
    }

    @Override
    public boolean exists(String identifier) {
        return users.stream()
                .anyMatch(u -> u.getUsername().equals(identifier) || u.getEmail().equals(identifier));
    }

    @Override
    public boolean existsEmail(String email) {
        return users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    private String serialize(User u) {
        return u.getUsername() + DELIMITER_OUT +
                u.getEmail() + DELIMITER_OUT +
                u.getPassword() + DELIMITER_OUT +
                u.getRuolo().name() + DELIMITER_OUT +
                u.isGoogleUser();
    }

    private User deserialize(String line) {
        String[] parts = line.split(DELIMITER);
        if (parts.length != 5) {
            return null;
        }
        try {
            String username = parts[0];
            String email = parts[1];
            String password = parts[2];
            UserRole ruolo = UserRole.valueOf(parts[3].toUpperCase());
            boolean isGoogleUser = Boolean.parseBoolean(parts[4]);
            User u = new User(username);
            u.setEmail(email);
            u.setPassword(password);
            u.setRuolo(ruolo);
            u.setGoogleUser(isGoogleUser);
            return u;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Errore nella deserializzazione della riga: {0}", line);
            return null;
        }
    }
}