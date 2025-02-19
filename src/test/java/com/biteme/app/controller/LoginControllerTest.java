package com.biteme.app.controller;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.entities.User;
import com.biteme.app.entities.UserRole;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.util.HashingUtil;
import com.biteme.app.util.UserSession;
import com.biteme.app.persistence.inmemory.Storage;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

//@author Lorenzo Passacantilli

class LoginControllerTest {

    private LoginController controller;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws Exception {
        controller = new LoginController();
        userDao = Configuration.getPersistenceProvider().getDaoFactory().getUserDao();

        injectMockUserDao();
    }

    private void injectMockUserDao() throws Exception {
        Field daoField = LoginController.class.getDeclaredField("userDao");
        daoField.setAccessible(true);
        daoField.set(controller, userDao);
    }

    @AfterEach
    void tearDown() {
        if (Configuration.getPersistenceProvider().getDaoFactory()
                instanceof com.biteme.app.persistence.inmemory.InMemoryDaoFactory) {
            Storage.getInstance().getUsers().clear();
        } else {
            try {
                userDao.delete("testuser");
            } catch (Exception e) {
                throw new RuntimeException("Errore durante la cancellazione dell'utente testuser", e);
            }
        }
        UserSession.clear();
    }

    @Test
    void testAuthenticateUserSuccess() {
        String password = "password123";
        User user = new User("testuser", "test@example.com", password, UserRole.CAMERIERE);
        user.setGoogleUser(false);

        userDao.store(user);

        LoginBean loginBean = createLoginBean("test@example.com", password);

        assertDoesNotThrow(() -> controller.authenticateUser(loginBean));

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
