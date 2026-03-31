package com.back.Backend.Service;

import com.back.Backend.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Spy
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(userService);
    }

    // Tests for getAllUsers()
    @Test
    void testGetAllUsers_WhenCalled_ThenReturnsAllUsers() {
        List<User> users = userService.getAllUsers();
        assertEquals(15, users.size(), "Should return 15 dummy users");
        
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetAllUsers_WhenCalled_ThenReturnsNonEmptyList() {
        List<User> users = userService.getAllUsers();
        assertFalse(users.isEmpty(), "Users list should not be empty");
        
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetAllUsers_WhenCalled_ThenReturnsCopyOfList() {
        List<User> users1 = userService.getAllUsers();
        List<User> users2 = userService.getAllUsers();
        assertNotSame(users1, users2, "Should return a copy, not the original list");
        assertEquals(users1.size(), users2.size(), "Both lists should have same size");
        
        verify(userService, times(2)).getAllUsers();
    }

    // Tests for getUserById()
    @Test
    void testGetUserById_WhenUserExists_ThenReturnsUser() {
        User user = userService.getUserById(1L);
        assertNotNull(user, "User with id 1 should exist");
        assertEquals("Shambhu", user.getName());
        assertEquals("shambhu@gmail.com", user.getEmail());
        
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void testGetUserById_WhenUserDoesNotExist_ThenReturnsNull() {
        User user = userService.getUserById(999L);
        assertNull(user, "User with id 999 should not exist");
        
        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    void testGetUserById_WhenIdIsNull_ThenReturnsNull() {
        User user = userService.getUserById(null);
        assertNull(user, "Should return null when id is null");
        
        verify(userService, times(1)).getUserById(null);
    }

    @Test
    void testGetUserById_WhenMultipleUsersExist_ThenReturnsCorrectUser() {
        User user = userService.getUserById(5L);
        assertNotNull(user);
        assertEquals("Amit", user.getName());
        
        verify(userService, times(1)).getUserById(5L);
    }

    @Test
    void testGetUserById_WhenIdIsNegative_ThenReturnsNull() {
        User user = userService.getUserById(-1L);
        assertNull(user, "Should return null for negative id");
        
        verify(userService, times(1)).getUserById(-1L);
    }

    // Tests for addUser()
    @Test
    void testAddUser_WhenNewUserAdded_ThenUserIsAddedToList() {
        User newUser = new User(20L, "TestUser", "test@gmail.com");
        User addedUser = userService.addUser(newUser);

        assertNotNull(addedUser, "Added user should not be null");
        assertEquals(newUser, addedUser, "Should return the added user");
        
        verify(userService, times(1)).addUser(newUser);
    }

    @Test
    void testAddUser_WhenMultipleUsersAdded_ThenAllAreStored() {
        int initialSize = userService.getAllUsers().size();
        
        User user1 = new User(20L, "User1", "user1@gmail.com");
        User user2 = new User(21L, "User2", "user2@gmail.com");
        
        userService.addUser(user1);
        userService.addUser(user2);

        List<User> users = userService.getAllUsers();
        assertEquals(initialSize + 2, users.size(), "Should have 2 more users");
        
        verify(userService, times(1)).addUser(user1);
        verify(userService, times(1)).addUser(user2);
    }

    @Test
    void testAddUser_WhenUserWithNullProperties_ThenUserIsStored() {
        User userWithNulls = new User(22L, null, null);
        User addedUser = userService.addUser(userWithNulls);

        assertNotNull(addedUser, "User with null properties should still be added");
        
        verify(userService, times(1)).addUser(userWithNulls);
    }

    @Test
    void testAddUser_WhenNullUserPassed_ThenNullIsStored() {
        assertDoesNotThrow(() -> {
            userService.addUser(null);
        }, "Should not throw exception when adding null user");
        
        verify(userService, times(1)).addUser(null);
    }

    // Tests for updateUser()
    @Test
    void testUpdateUser_WhenUserExists_ThenUserIsUpdated() {
        User updateData = new User(1L, "UpdatedShambhu", "updated@gmail.com");
        User updatedUser = userService.updateUser(1L, updateData);

        assertNotNull(updatedUser, "Updated user should not be null");
        assertEquals("UpdatedShambhu", updatedUser.getName());
        assertEquals("updated@gmail.com", updatedUser.getEmail());
        
        verify(userService, times(1)).updateUser(1L, updateData);
    }

    @Test
    void testUpdateUser_WhenUserDoesNotExist_ThenReturnsNull() {
        User updateData = new User(999L, "NonExistent", "nonexistent@gmail.com");
        User result = userService.updateUser(999L, updateData);

        assertNull(result, "Should return null when user doesn't exist");
        
        verify(userService, times(1)).updateUser(999L, updateData);
    }

    @Test
    void testUpdateUser_WhenIdIsNull_ThenReturnsNull() {
        User updateData = new User(1L, "Test", "test@gmail.com");
        User result = userService.updateUser(null, updateData);

        assertNull(result, "Should return null when id is null");
        
        verify(userService, times(1)).updateUser(null, updateData);
    }

    @Test
    void testUpdateUser_WhenUpdateDataIsNull_ThenThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            userService.updateUser(1L, null);
        }, "Should throw NullPointerException when update data is null");
        
        verify(userService, times(1)).updateUser(1L, null);
    }

    @Test
    void testUpdateUser_WhenPartiallyUpdateUser_ThenOnlyNameAndEmailUpdated() {
        Long userId = 1L;
        User updateData = new User(999L, "NewName", "newemail@gmail.com");
        userService.updateUser(userId, updateData);
        
        User afterUpdate = userService.getUserById(userId);
        assertEquals("NewName", afterUpdate.getName());
        assertEquals("newemail@gmail.com", afterUpdate.getEmail());
        assertEquals(userId, afterUpdate.getId(), "ID should remain unchanged");
        
        verify(userService, times(1)).updateUser(userId, updateData);
    }

    @Test
    void testUpdateUser_WhenUpdateWithEmptyStrings_ThenUpdatedWithEmpty() {
        User updateData = new User(1L, "", "");
        User result = userService.updateUser(1L, updateData);

        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals("", result.getEmail());
        
        verify(userService, times(1)).updateUser(1L, updateData);
    }

    // Tests for deleteUser()
    @Test
    void testDeleteUser_WhenUserExists_ThenUserIsDeleted() {
        Long userIdToDelete = 15L;
        User userBefore = userService.getUserById(userIdToDelete);
        assertNotNull(userBefore, "User should exist before deletion");

        boolean deleted = userService.deleteUser(userIdToDelete);
        assertTrue(deleted, "Should return true when user is deleted");

        User userAfter = userService.getUserById(userIdToDelete);
        assertNull(userAfter, "User should not exist after deletion");
        
        verify(userService, times(1)).deleteUser(userIdToDelete);
    }

    @Test
    void testDeleteUser_WhenUserDoesNotExist_ThenReturnsFalse() {
        boolean deleted = userService.deleteUser(999L);
        assertFalse(deleted, "Should return false when user doesn't exist");
        
        verify(userService, times(1)).deleteUser(999L);
    }

    @Test
    void testDeleteUser_WhenIdIsNull_ThenReturnsFalse() {
        boolean deleted = userService.deleteUser(null);
        assertFalse(deleted, "Should return false when id is null");
        
        verify(userService, times(1)).deleteUser(null);
    }

    @Test
    void testDeleteUser_WhenMultipleUsersDeleted_ThenAllAreRemoved() {
        int initialSize = userService.getAllUsers().size();
        
        userService.deleteUser(1L);
        userService.deleteUser(2L);

        List<User> users = userService.getAllUsers();
        assertEquals(initialSize - 2, users.size(), "Should have 2 fewer users");
        
        verify(userService, times(1)).deleteUser(1L);
        verify(userService, times(1)).deleteUser(2L);
    }

    @Test
    void testDeleteUser_WhenSameUserDeletedTwice_ThenSecondReturnsFalse() {
        boolean firstDelete = userService.deleteUser(14L);
        boolean secondDelete = userService.deleteUser(14L);

        assertTrue(firstDelete, "First delete should succeed");
        assertFalse(secondDelete, "Second delete should fail");
        
        verify(userService, times(2)).deleteUser(14L);
    }

    // Tests for searchUsers()
    @Test
    void testSearchUsers_WhenSearchByName_ThenReturnsMatchingUsers() {
        List<User> results = userService.searchUsers("Amit", "");
        assertFalse(results.isEmpty(), "Should find users matching name");
        assertTrue(results.stream().anyMatch(u -> u.getName().contains("Amit")));
        
        verify(userService, times(1)).searchUsers("Amit", "");
    }

    @Test
    void testSearchUsers_WhenSearchByEmail_ThenIgnoresEmailParameter() {
        List<User> results = userService.searchUsers("gmail", "");
        assertFalse(results.isEmpty(), "Should find users with 'gmail' in email via query parameter");
        
        verify(userService, times(1)).searchUsers("gmail", "");
    }

    @Test
    void testSearchUsers_WhenSearchByPartialName_ThenReturnsMatchingUsers() {
        List<User> results = userService.searchUsers("Raj", "");
        assertFalse(results.isEmpty(), "Should find users with partial name match");
        
        verify(userService, times(1)).searchUsers("Raj", "");
    }

    @Test
    void testSearchUsers_WhenSearchByPartialEmail_ThenReturnsMatchingUsers() {
        List<User> results = userService.searchUsers("", "gmail");
        assertFalse(results.isEmpty(), "Should find users with partial email match");
        
        verify(userService, times(1)).searchUsers("", "gmail");
    }

    @Test
    void testSearchUsers_WhenNoMatch_ThenReturnsEmptyList() {
        List<User> results = userService.searchUsers("NonExistentUser", "");
        assertTrue(results.isEmpty(), "Should return empty list when no match");
        
        verify(userService, times(1)).searchUsers("NonExistentUser", "");
    }

    @Test
    void testSearchUsers_WhenSearchIsEmpty_ThenReturnsAllUsers() {
        List<User> results = userService.searchUsers("", "");
        assertFalse(results.isEmpty(), "Should return all users when search query is empty string");
        assertEquals(userService.getAllUsers().size(), results.size());
        
        verify(userService, times(1)).searchUsers("", "");
    }

    @Test
    void testSearchUsers_WhenSearchIsCaseInsensitive_ThenReturnsResults() {
        List<User> resultsLower = userService.searchUsers("amit", "");
        List<User> resultsUpper = userService.searchUsers("AMIT", "");
        List<User> resultsMixed = userService.searchUsers("AmIt", "");

        assertFalse(resultsLower.isEmpty(), "Lowercase search should work");
        assertFalse(resultsUpper.isEmpty(), "Uppercase search should work");
        assertFalse(resultsMixed.isEmpty(), "Mixed case search should work");
        
        verify(userService, times(1)).searchUsers("amit", "");
        verify(userService, times(1)).searchUsers("AMIT", "");
        verify(userService, times(1)).searchUsers("AmIt", "");
    }

    @Test
    void testSearchUsers_WhenSearchWithNullQuery_ThenThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            userService.searchUsers(null, "");
        }, "Should throw NullPointerException for null query");
        
        verify(userService, times(1)).searchUsers(null, "");
    }

    @Test
    void testSearchUsers_WhenSearchWithNullEmail_ThenIgnoresEmailParameter() {
        List<User> results = userService.searchUsers("Amit", null);
        assertFalse(results.isEmpty(), "Should find results even with null email since email parameter is unused");
        assertTrue(results.stream().anyMatch(u -> u.getName().contains("Amit")));
        
        verify(userService, times(1)).searchUsers("Amit", null);
    }

    @Test
    void testSearchUsers_WhenMultipleMatches_ThenReturnsAllMatches() {
        List<User> results = userService.searchUsers("a", "");
        assertTrue(results.size() > 1, "Should return multiple matches");
        
        verify(userService, times(1)).searchUsers("a", "");
    }

    @Test
    void testSearchUsers_WhenSearchMatchesMultipleFields_ThenReturnsResult() {
        List<User> results = userService.searchUsers("h", "");
        assertFalse(results.isEmpty(), "Should find users matching the search criteria");
        
        verify(userService, times(1)).searchUsers("h", "");
    }
}

