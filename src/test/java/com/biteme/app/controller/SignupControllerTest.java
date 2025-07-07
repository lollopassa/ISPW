package com.biteme.app.controller;

import com.biteme.app.bean.SignupBean;
import com.biteme.app.entities.User;
import com.biteme.app.persistence.Configuration;
import com.biteme.app.persistence.UserDao;
import com.biteme.app.persistence.inmemory.Storage;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SignupControllerTest {

    private SignupController controller;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        controller = new SignupController();
        userDao = Configuration.getPersistenceProvider()
                .getDaoFactory()
                .getUserDao();
        clearStorage();
    }

    @AfterEach
    void tearDown() {
        clearStorage();
    }

    private void clearStorage() {
        if (Configuration.getPersistenceProvider().getDaoFactory()
                instanceof com.biteme.app.persistence.inmemory.InMemoryDaoFactory) {
            Storage.getInstance().getUsers().clear();
        }
    }

    @Test
    void registerUser_success() {
        SignupBean bean = new SignupBean();
        bean.setUsername("user1");
        bean.setEmail("user1@example.com");
        bean.setPassword("secret");
        bean.setConfirmPassword("secret");

        assertDoesNotThrow(() -> controller.registerUser(bean));

        User u = userDao.read("user1").orElseThrow();
        // Password is stored as SHA-256 hex
        String expectedHash = "2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b";
        assertEquals(expectedHash, u.getPassword());
    }

    @Test
    void registerUser_duplicateEmail_throws() {
        SignupBean first = new SignupBean();
        first.setUsername("alice");
        first.setEmail("alice@example.com");
        first.setPassword("pw1");
        first.setConfirmPassword("pw1");
        controller.registerUser(first);

        SignupBean dupEmail = new SignupBean();
        dupEmail.setUsername("bob");
        dupEmail.setEmail("alice@example.com");
        dupEmail.setPassword("pw2");
        dupEmail.setConfirmPassword("pw2");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.registerUser(dupEmail)
        );
        assertEquals("Email già registrata", ex.getMessage());
    }

    @Test
    void registerUser_duplicateUsername_throws() {
        SignupBean first = new SignupBean();
        first.setUsername("charlie");
        first.setEmail("charlie@example.com");
        first.setPassword("pw1");
        first.setConfirmPassword("pw1");
        controller.registerUser(first);

        SignupBean dupUser = new SignupBean();
        dupUser.setUsername("charlie");
        dupUser.setEmail("charlie2@example.com");
        dupUser.setPassword("pw2");
        dupUser.setConfirmPassword("pw2");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.registerUser(dupUser)
        );
        assertEquals("Username già registrato", ex.getMessage());
    }

    @Test
    void navigateToLogin_throwsIfNotInitialized() {
        assertThrows(
                IllegalStateException.class,
                () -> controller.navigateToLogin()
        );
    }
}
