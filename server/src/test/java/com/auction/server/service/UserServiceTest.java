package com.auction.server.service;

import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.mapper.UserMapper;
import com.auction.server.util.PasswordUtil;
import com.auction.share.exceptions.AuthenticationException;
import com.auction.share.exceptions.DuplicateResourceException;
import com.auction.share.DTO.UpdateProfileRequest;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.AfterEach;
import com.auction.server.util.DatabaseConnection;
import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import java.sql.Connection;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private BidTransactionDAO bidTransactionDAO;

    @Mock
    private com.auction.server.dao.AuctionDAO auctionDAO;

    private UserMapper userMapper;

    private UserService userService;

    private Connection mockConn;

    @BeforeEach
    void setUp() throws Exception {
        userMapper = new UserMapper();
        userService = new UserService(userDAO, bidTransactionDAO, auctionDAO, userMapper);
        mockConn = mock(Connection.class);
        DatabaseConnection.setTestConnection(mockConn);
    }

    @AfterEach
    void tearDown() {
        DatabaseConnection.setTestConnection(null);
    }

    @Test
    void register_duplicateUsername_throwsDuplicate() throws Exception {
        Bidder user = bidder("alice", "plain-pass", "alice@mail.com");
        when(userDAO.isUsernameTaken(any(Connection.class), eq("alice"))).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(user));
    }

    @Test
    void register_duplicateEmail_throwsDuplicate() throws Exception {
        Bidder user = bidder("alice", "plain-pass", "alice@mail.com");
        when(userDAO.isUsernameTaken(any(Connection.class), eq("alice"))).thenReturn(false);
        when(userDAO.isEmailTaken(any(Connection.class), eq("alice@mail.com"))).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(user));
    }

    @Test
    void register_success_passwordIsHashed() throws Exception {
        Bidder user = bidder("alice", "plain-pass", "alice@mail.com");
        when(userDAO.isUsernameTaken(any(Connection.class), eq("alice"))).thenReturn(false);
        when(userDAO.isEmailTaken(any(Connection.class), eq("alice@mail.com"))).thenReturn(false);
        when(userDAO.saveUser(any(Connection.class), eq(user))).thenReturn(true);

        boolean saved = userService.register(user);

        assertTrue(saved);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).saveUser(any(Connection.class), captor.capture());
        assertNotEquals("plain-pass", captor.getValue().getPassword());
    }

    @Test
    void login_userNotFound_throwsAuthentication() throws Exception {
        when(userDAO.findByUsername(any(Connection.class), eq("missing"))).thenReturn(null);

        assertThrows(AuthenticationException.class, () -> userService.login("missing", "any"));
    }

    @Test
    void login_wrongPassword_throwsAuthentication() throws Exception {
        Bidder user = bidder("alice", PasswordUtil.hashPassword("correct-pass"), "alice@mail.com");
        when(userDAO.findByUsername(any(Connection.class), eq("alice"))).thenReturn(user);

        assertThrows(AuthenticationException.class, () -> userService.login("alice", "wrong-pass"));
    }

    @Test
    void login_success_returnsUser() throws Exception {
        Bidder user = bidder("alice", PasswordUtil.hashPassword("plain-pass"), "alice@mail.com");
        when(userDAO.findByUsername(any(Connection.class), eq("alice"))).thenReturn(user);

        User loggedIn = userService.login("alice", "plain-pass");

        assertSame(user, loggedIn);
    }

    @Test
    void updateProfile_success_usesTransaction() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("u-1", "Alice New", "pass", "0123", "new@mail.com", "New Address");
        User mockUser = bidder("alice", "pass", "new@mail.com");
        when(userDAO.findById(any(Connection.class), eq("u-1"))).thenReturn(mockUser);

        User updated = userService.updateProfile(request);

        assertSame(mockUser, updated);
        verify(mockConn).setAutoCommit(false);
        verify(userDAO).updateProfile(any(Connection.class), eq("u-1"), eq("Alice New"), eq("new@mail.com"), eq("New Address"), eq("0123"), anyString());
        verify(mockConn).commit();
        verify(mockConn).setAutoCommit(true);
    }

    @Test
    void updateProfile_failure_rollbacks() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("u-1", "Alice New", "pass", "0123", "new@mail.com", "New Address");
        when(userDAO.findById(any(Connection.class), eq("u-1"))).thenReturn(null);

        assertThrows(ValidationException.class, () -> userService.updateProfile(request));

        verify(mockConn).setAutoCommit(false);
        verify(mockConn).rollback();
        verify(mockConn).setAutoCommit(true);
    }

    private static Bidder bidder(String username, String password, String email) {
        return new Bidder(username, password, "Alice", "090", email, "HCM");
    }
}
