package com.biteme.app.controller;

import com.biteme.app.bean.LoginBean;
import com.biteme.app.model.User;
import com.biteme.app.model.UserRole;
import com.biteme.app.persistence.inmemory.InMemoryUserDao;
import com.biteme.app.persistence.inmemory.Storage;
import com.biteme.app.util.HashingUtil;
import com.biteme.app.util.UserSession;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

//@author Lorenzo Passacantilli


class LoginControllerTest {

    private LoginController controller;
    private InMemoryUserDao userDao;

    @BeforeEach
    void setUp() throws Exception {
        controller = new LoginController();
        userDao = new InMemoryUserDao();
        injectMockUserDao();
    }

    private void injectMockUserDao() throws Exception {
        Field daoField = LoginController.class.getDeclaredField("userDao");
        daoField.setAccessible(true);
        daoField.set(controller, userDao);
    }

    @AfterEach
    void tearDown() {
        Storage.getInstance().getUsers().clear();
        UserSession.clear();
    }

    @Test
    void testAuthenticateUserSuccess() {
        // Crea un utente con password in chiaro (il sistema si occuperà dell'hashing)
        String password = "password123";
        User user = new User("testuser", "test@example.com", password, UserRole.CAMERIERE);
        user.setGoogleUser(false);
        userDao.store(user); // Assicurati che lo store faccia l'hashing della password

        // Crea un LoginBean con la password in chiaro
        LoginBean loginBean = createLoginBean("test@example.com", password);

        // Esegui l'autenticazione
        assertDoesNotThrow(() -> controller.authenticateUser(loginBean));
        assertEquals(user, UserSession.getCurrentUser());
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
        user.setGoogleUser(false); // Imposta googleUser
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