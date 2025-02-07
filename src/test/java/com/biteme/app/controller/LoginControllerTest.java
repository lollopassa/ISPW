package com.biteme.app.controller;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.exception.GoogleAuthException;
import com.biteme.app.model.User;
import com.biteme.app.model.UserRole;
import com.biteme.app.persistence.inmemory.InMemoryUserDao;
import com.biteme.app.persistence.inmemory.Storage;
import com.biteme.app.service.GoogleAuthService;
import com.biteme.app.util.GoogleAuthUtility; // Import corretto
import com.biteme.app.util.HashingUtil;
import com.biteme.app.util.UserSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

//@author Lorenzo Passacantilli


class LoginControllerTest {

    private LoginController controller;
    private InMemoryUserDao inMemoryUserDao;
    private StubGoogleAuthService stubGoogleAuthService;

    // Classe stub per GoogleAuthService
    static class StubGoogleAuthService extends GoogleAuthService {
        private boolean authenticationSuccess = true;
        private GoogleAuthUtility.GoogleUserData userData;

        public void setUserData(GoogleAuthUtility.GoogleUserData userData) {
            this.userData = userData;
        }

        @Override
        public String authenticateWithGoogle() throws GoogleAuthException {
            if (!authenticationSuccess) throw new GoogleAuthException("Authentication failed");
            return "dummy_token";
        }

        @Override
        public GoogleAuthUtility.GoogleUserData getGoogleUserData(String accessToken) {
            return userData;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new LoginController();
        inMemoryUserDao = new InMemoryUserDao();
        stubGoogleAuthService = new StubGoogleAuthService();

        // Inject dependencies via reflection
        Field userDaoField = LoginController.class.getDeclaredField("userDao");
        userDaoField.setAccessible(true);
        userDaoField.set(controller, inMemoryUserDao);

        Field googleAuthField = LoginController.class.getDeclaredField("googleAuthService");
        googleAuthField.setAccessible(true);
        googleAuthField.set(controller, stubGoogleAuthService);
    }

    @AfterEach
    void tearDown() {
        UserSession.clear();
        Storage.getInstance().getUsers().clear();
    }

    @Test
    void testAuthenticateUser_WithEmptyEmailOrPassword() {
        // Caso 1: Email vuota
        LoginBean bean = new LoginBean();
        bean.setEmailOrUsername("");
        bean.setPassword("password");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.authenticateUser(bean));
        assertEquals("Email/Username e password sono obbligatori.", ex.getMessage());

        // Caso 2: Password vuota
        bean.setEmailOrUsername("test@test.com");
        bean.setPassword("");

        ex = assertThrows(IllegalArgumentException.class, () -> controller.authenticateUser(bean));
        assertEquals("Email/Username e password sono obbligatori.", ex.getMessage());

        // Caso 3: Entrambi i campi vuoti
        bean.setEmailOrUsername("");
        bean.setPassword("");

        ex = assertThrows(IllegalArgumentException.class, () -> controller.authenticateUser(bean));
        assertEquals("Email/Username e password sono obbligatori.", ex.getMessage());
    }

    @Test
    void testAuthenticateUser_WithInvalidEmail() {
        LoginBean bean = new LoginBean();
        bean.setEmailOrUsername("invalid-email"); // Email senza "@" o formato sbagliato
        bean.setPassword("password");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.authenticateUser(bean));
        assertEquals("Il formato dell'email non è valido.", ex.getMessage());
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        LoginBean bean = new LoginBean();
        bean.setEmailOrUsername("nonexistent@test.com");
        bean.setPassword("password");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.authenticateUser(bean));
        assertEquals("Utente non trovato.", ex.getMessage());
    }

    @Test
    void testAuthenticateUser_AsGoogleUser() {
        // Crea un utente Google e aggiungilo in memoria
        User user = new User("googleuser", "google@test.com", null, UserRole.CAMERIERE);
        user.setGoogleUser(true);
        inMemoryUserDao.store(user);

        LoginBean bean = new LoginBean();
        bean.setEmailOrUsername("google@test.com");
        bean.setPassword("anypass");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.authenticateUser(bean));
        assertEquals("Utente Google non può autenticarsi tramite metodo tradizionale.", ex.getMessage());
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        // Crea un utente con password corretta e aggiungilo in memoria
        User user = new User("testuser", "test@test.com", HashingUtil.hashPassword("correctpassword"), UserRole.CAMERIERE);
        inMemoryUserDao.store(user);

        LoginBean bean = new LoginBean();
        bean.setEmailOrUsername("test@test.com");
        bean.setPassword("wrongpassword");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.authenticateUser(bean));
        assertEquals("Password errata.", ex.getMessage());
    }

    @Test
    void testAuthenticateWithGoogle_MissingGoogleUserData()  {
        // Simula uno scenario in cui i dati dell'utente Google risultino null
        stubGoogleAuthService.setUserData(null);

        Exception ex = assertThrows(IllegalStateException.class, () -> controller.authenticateWithGoogle());
        assertEquals("Dati utente Google non validi.", ex.getMessage());
    }

    @Test
    void testAuthenticateWithGoogle_UserAlreadyExistsWithPassword() {
        // Simula un utente che esiste con un metodo tradizionale (non Google)
        User user = new User("testuser", "test@test.com", "hashedpassword", UserRole.CAMERIERE);
        user.setGoogleUser(false);
        inMemoryUserDao.store(user);

        // Simula che Google restituisca i dati di questo utente
        stubGoogleAuthService.setUserData(new GoogleAuthUtility.GoogleUserData("test@test.com", "Test User"));

        Exception ex = assertThrows(IllegalStateException.class, () -> controller.authenticateWithGoogle());
        assertEquals("Email già registrata con metodo tradizionale", ex.getMessage());
    }

    @Test
    void testNavigateToHome_InvalidUser() {
        // Simula che nessun utente è loggato
        UserSession.clear();

        Exception ex = assertThrows(NullPointerException.class, () -> controller.navigateToHome());
        assertEquals("Utente non trovato.", ex.getMessage()); // Controlla il caso limite
    }

    @Test
    void testNavigateToHome_ValidRoles() {
        // Caso utente Admin
        User admin = new User("admin", "admin@test.com", "password", UserRole.ADMIN);
        UserSession.setCurrentUser(admin);

        assertDoesNotThrow(() -> controller.navigateToHome());

        // Caso utente Cameriere
        User cameriere = new User("user", "user@test.com", "password", UserRole.CAMERIERE);
        UserSession.setCurrentUser(cameriere);

        assertDoesNotThrow(() -> controller.navigateToHome());
    }

    @Test
    void testLogout() {
        // Simula che un utente sia loggato
        User user = new User("user", "user@test.com", "password", UserRole.CAMERIERE);
        UserSession.setCurrentUser(user);

        // Chiama il logout
        controller.logout();

        // Verifica che l'utente corrente sia stato rimosso
        assertNull(UserSession.getCurrentUser());
    }

    @Test
    void testGetCurrentUsername_WhenUserIsNotLoggedIn() {
        // Verifica cosa succede quando nessun utente è loggato
        UserSession.clear();
        assertEquals("", controller.getCurrentUsername());
    }


}