package com.biteme.app.persistence.database;

import com.biteme.app.entity.User;
import com.biteme.app.entity.UserRole;
import com.biteme.app.exception.*;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.util.HashingUtil;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUserDao implements UserDao {

    private static final Logger LOGGER = Logger.getLogger(DatabaseUserDao.class.getName());

    private Connection connection;

    public DatabaseUserDao() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la connessione al database", e);
        }
    }

    @Override
    public Optional<User> load(String identifier) {
        String query = "SELECT username, email, passwordHash, ruolo FROM user WHERE username = ? OR email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("passwordHash"));
                    String ruolo = rs.getString("ruolo");
                    if (ruolo != null) {
                        user.setRuolo(UserRole.fromString(ruolo));
                    }
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento dell'utente", e);
        }
        return Optional.empty();
    }

    @Override
    public void store(User user) {
        try {
            if (existsEmail(user.getEmail())) {
                throw new SignupException("Email già registrata");
            }

            try (Statement stmtCount = connection.createStatement();
                 ResultSet rs = stmtCount.executeQuery("SELECT COUNT(*) FROM user")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    user.setRuolo(UserRole.ADMIN);
                } else if (user.getRuolo() == null) {
                    user.setRuolo(UserRole.CAMERIERE);
                }
            }

            String hashedPassword = HashingUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);

            String query = "INSERT INTO user (username, email, passwordHash, ruolo) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, hashedPassword);
                stmt.setString(4, user.getRuolo().name());
                stmt.executeUpdate();
                LOGGER.log(Level.INFO, "Utente {0} registrato con successo", user.getUsername());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio dell'utente", e);
        }
    }

    @Override
    public void delete(String key) {
        String query = "DELETE FROM user WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, key);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.log(Level.WARNING, "Nessun utente trovato con username: {0}", key);
            } else {
                LOGGER.log(Level.INFO, "Utente {0} eliminato con successo", key);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'eliminazione dell'utente", e);
        }
    }

    @Override
    public boolean exists(String identifier) {
        String query = "SELECT COUNT(*) FROM user WHERE username = ? OR email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la verifica dell'esistenza dell'utente", e);
            return false;
        }
    }

    public boolean existsEmail(String email) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM user WHERE email = ?")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la verifica dell'email", e);
        }
        return false; // Return false if there's an exception during the check
    }


    public boolean existsRole(UserRole role) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM user WHERE ruolo = ?")) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la verifica del ruolo", e);
        }
        return false; // Return false if there's an exception during the check
    }
}