package com.auction.server.service;

import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.util.PasswordUtil;
import com.auction.share.exceptions.AuthenticationException;
import com.auction.share.exceptions.DuplicateResourceException;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private BidTransactionDAO bidTransactionDAO;

    private UserService userService;

    @BeforeEach
    void setUp() throws Exception {
        userService = new UserService(userDAO);
        Field field = UserService.class.getDeclaredField("bidTransactionDAO");
        field.setAccessible(true);
        field.set(userService, bidTransactionDAO);
    }

    @Test
    void register_duplicateUsername_throwsDuplicate() throws Exception {
        Bidder user = bidder("alice", "plain-pass", "alice@mail.com");
        when(userDAO.isUsernameTaken("alice")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(user));
    }

    @Test
    void register_duplicateEmail_throwsDuplicate() throws Exception {
        Bidder user = bidder("alice", "plain-pass", "alice@mail.com");
        when(userDAO.isUsernameTaken("alice")).thenReturn(false);
        when(userDAO.isEmailTaken("alice@mail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(user));
    }

    @Test
    void register_success_passwordIsHashed() throws Exception {
        Bidder user = bidder("alice", "plain-pass", "alice@mail.com");
        when(userDAO.isUsernameTaken("alice")).thenReturn(false);
        when(userDAO.isEmailTaken("alice@mail.com")).thenReturn(false);
        when(userDAO.saveUser(user)).thenReturn(true);

        boolean saved = userService.register(user);

        assertTrue(saved);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).saveUser(captor.capture());
        assertNotEquals("plain-pass", captor.getValue().getPassword());
    }

    @Test
    void login_userNotFound_throwsAuthentication() throws Exception {
        when(userDAO.findByUsername("missing")).thenReturn(null);

        assertThrows(AuthenticationException.class, () -> userService.login("missing", "any"));
    }

    @Test
    void login_wrongPassword_throwsAuthentication() throws Exception {
        Bidder user = bidder("alice", PasswordUtil.hashPassword("correct-pass"), "alice@mail.com");
        when(userDAO.findByUsername("alice")).thenReturn(user);

        assertThrows(AuthenticationException.class, () -> userService.login("alice", "wrong-pass"));
    }

    @Test
    void login_success_returnsUser() throws Exception {
        Bidder user = bidder("alice", PasswordUtil.hashPassword("plain-pass"), "alice@mail.com");
        when(userDAO.findByUsername("alice")).thenReturn(user);

        User loggedIn = userService.login("alice", "plain-pass");

        assertSame(user, loggedIn);
    }

    @Test
    void updateFullName_success_returnTrue() throws Exception {
        when(userDAO.updateUserFullName(anyString(), anyString())).thenReturn(true);

        boolean updated = userService.updateFullName("u-1", "Alice Updated");

        assertTrue(updated);
        verify(userDAO).updateUserFullName("u-1", "Alice Updated");
    }

    private static Bidder bidder(String username, String password, String email) {
        return new Bidder(username, password, "Alice", "090", email, "HCM");
    }
}
