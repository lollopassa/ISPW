package com.biteme.app.controller;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.model.User;
import com.biteme.app.model.UserRole;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.util.Configuration;
import com.biteme.app.util.HashingUtil;
import com.biteme.app.util.UserSession;
import com.biteme.app.persistence.inmemory.Storage; // Solo per cleanup in memory
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    private LoginController controller;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws Exception {
        controller = new LoginController();
        userDao = Configuration.getPersistenceProvider().getDaoFactory().getUserDao();
        // Inietta la stessa istanza di UserDao nel controller, per avere dati condivisi
        injectMockUserDao();
    }

    private void injectMockUserDao() throws Exception {
        Field daoField = LoginController.class.getDeclaredField("userDao");
        daoField.setAccessible(true);
        daoField.set(controller, userDao);
    }

    @AfterEach
    void tearDown() {
        // Se si usa la persistenza in memory, pulisci lo storage.
        // Per txt e database non si svuota l'intero archivio per non eliminare dati persistenti.
        if (Configuration.getPersistenceProvider().getDaoFactory()
                instanceof com.biteme.app.persistence.inmemory.InMemoryDaoFactory) {
            Storage.getInstance().getUsers().clear();
        }
        UserSession.clear();
    }

    @Test
    void testAuthenticateUserSuccess() {
        // Controlla se esiste già un utente con l'email "test@example.com"
        // Se esiste, lo elimina tramite il delete (utilizzando il campo username come chiave).
        userDao.load("test@example.com").ifPresent(existingUser ->
                userDao.delete(existingUser.getUsername())
        );

        // Creiamo un utente: la password viene passata in chiaro,
        // il sistema si occuperà dell'hashing quando verificherà l'autenticazione.
        String password = "password123";
        User user = new User("testuser", "test@example.com", password, UserRole.CAMERIERE);
        user.setGoogleUser(false);

        // Salviamo l'utente tramite la DAO generica (la stessa iniettata nel controller)
        userDao.store(user);

        // Creiamo un LoginBean con le credenziali in chiaro
        LoginBean loginBean = createLoginBean("test@example.com", password);

        // Eseguiamo l'autenticazione
        assertDoesNotThrow(() -> controller.authenticateUser(loginBean));

        // Dal momento che non abbiamo ridefinito equals() in User, confrontiamo i campi chiave.
        User actualUser = UserSession.getCurrentUser();
        assertNotNull(actualUser, "L'utente corrente non dovrebbe essere nullo");
        assertEquals(user.getUsername(), actualUser.getUsername(), "Username non corrispondente");
        assertEquals(user.getEmail(), actualUser.getEmail(), "Email non corrispondente");
        assertEquals(user.getRuolo(), actualUser.getRuolo(), "Ruolo utente non corrispondente");
        assertEquals(user.isGoogleUser(), actualUser.isGoogleUser(), "Flag Google non corrispondente");
    }

    @Test
    void testAuthenticateUserInvalidEmailFormat() {
        LoginBean loginBean = createLoginBean("invalid@format", "password123");
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.authenticateUser(loginBean)
        );
        assertEquals("Il formato dell'email non è valido.", exception.getMessage());
    }

    @Test
    void testNavigateToHomeAsAdmin() {
        User user = new User("admin", "admin@example.com", "admin123", UserRole.ADMIN);
        UserSession.setCurrentUser(user);
        assertEquals("/com/biteme/app/adminHome.fxml", controller.getHomeScenePath());
    }

    @Test
    void testNavigateToHomeAsUser() {
        User user = new User("user", "user@example.com", "user123", UserRole.CAMERIERE);
        UserSession.setCurrentUser(user);
        assertEquals("/com/biteme/app/home.fxml", controller.getHomeScenePath());
    }

    @Test
    void testLogout() {
        User user = new User("user", "user@example.com", HashingUtil.hashPassword("user123"), UserRole.CAMERIERE);
        user.setGoogleUser(false);
        UserSession.setCurrentUser(user);
        controller.logout();
        assertNull(UserSession.getCurrentUser());
    }

    private LoginBean createLoginBean(String email, String password) {
        LoginBean bean = new LoginBean();
        bean.setEmailOrUsername(email);
        bean.setPassword(password);
        return bean;
    }
}
