package com.back.Backend.Service;

import com.back.Backend.Model.User;
import com.back.Backend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private List<User> mockUsers;

    @BeforeEach
    void setUp() {
        mockUsers = List.of(
            new User(1L, "Shambhu", "shambhu@test.com"),
            new User(2L, "Rahul", "rahul@gmail.com"),
            new User(3L, "Suresh", "Suresh@gmail.com"),
            new User(4L, "Ramesh", "Ramesh@gmail.com"),
            new User(5L, "Amit", "amit@gmail.com"),
            new User(6L, "Priya", "priya@gmail.com"),
            new User(7L, "Vikram", "vikram@gmail.com"),
            new User(8L, "Neha", "neha@gmail.com"),
            new User(9L, "Rajesh", "rajesh@gmail.com"),
            new User(10L, "Anjali", "anjali@gmail.com"),
            new User(11L, "Sunil", "sunil@gmail.com"),
            new User(12L, "Pooja", "pooja@test.com"),
            new User(13L, "Manish", "manish@gmail.com"),
            new User(14L, "Kavita", "kavita@gmail.com"),
            new User(15L, "Sanjay", "sanjay@gmail.com"),
            new User(16L, "Ritu", "Ritu@yaho.com")
        );

        when(userRepository.findAll()).thenReturn(mockUsers);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUsers.get(0)));
        when(userRepository.findById(5L)).thenReturn(Optional.of(mockUsers.get(4)));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(userRepository.findById(null)).thenReturn(Optional.empty());
        when(userRepository.findById(-1L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(userRepository.existsById(999L)).thenReturn(false);
        when(userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(anyString(), anyString())).thenReturn(mockUsers);
    }

    // Tests for getAllUsers()
    @Test
    void testGetAllUsers_WhenCalled_ThenReturnsAllUsers() {
        List<User> users = userService.getAllUsers();
        assertEquals(15, users.size(), "Should return 15 dummy users");
        
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetAllUsers_WhenCalled_ThenReturnsNonEmptyList() {
        List<User> users = userService.getAllUsers();
        assertFalse(users.isEmpty(), "Users list should not be empty");
        
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetAllUsers_WhenCalled_ThenReturnsCopyOfList() {
        List<User> users1 = userService.getAllUsers();
        List<User> users2 = userService.getAllUsers();
        assertNotSame(users1, users2, "Should return a copy, not the original list");
        assertEquals(users1.size(), users2.size(), "Both lists should have same size");
        
        verify(userRepository, times(2)).findAll();
    }

    // Tests for getUserById()
    @Test
    void testGetUserById_WhenUserExists_ThenReturnsUser() {
        User user = userService.getUserById(1L);
        assertNotNull(user, "User with id 1 should exist");
        assertEquals("Shambhu", user.getName());
        assertEquals("shambhu@test.com", user.getEmail());
        
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_WhenUserDoesNotExist_ThenReturnsNull() {
        User user = userService.getUserById(999L);
        assertNull(user, "User with id 999 should not exist");
        
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void testGetUserById_WhenIdIsNull_ThenReturnsNull() {
        User user = userService.getUserById(null);
        assertNull(user, "Should return null when id is null");
        
        verify(userRepository, times(1)).findById(null);
    }

    @Test
    void testGetUserById_WhenMultipleUsersExist_ThenReturnsCorrectUser() {
        User user = userService.getUserById(5L);
        assertNotNull(user);
        assertEquals("Amit", user.getName());
        
        verify(userRepository, times(1)).findById(5L);
    }

    @Test
    void testGetUserById_WhenIdIsNegative_ThenReturnsNull() {
        User user = userService.getUserById(-1L);
        assertNull(user, "Should return null for negative id");
        
        verify(userRepository, times(1)).findById(-1L);
    }

    // Tests for addUser()
    @Test
    void testAddUser_WhenNewUserAdded_ThenUserIsAddedToList() {
        User newUser = new User(20L, "TestUser", "test@gmail.com");
        User addedUser = userService.addUser(newUser);

        assertNotNull(addedUser, "Added user should not be null");
        assertEquals(newUser, addedUser, "Should return the added user");
        
        verify(userRepository, times(1)).save(newUser);
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
        
        verify(userRepository, times(1)).save(user1);
        verify(userRepository, times(1)).save(user2);
    }

    @Test
    void testAddUser_WhenUserWithNullProperties_ThenUserIsStored() {
        User userWithNulls = new User(22L, null, null);
        User addedUser = userService.addUser(userWithNulls);

        assertNotNull(addedUser, "User with null properties should still be added");
        
        verify(userRepository, times(1)).save(userWithNulls);
    }

    @Test
    void testAddUser_WhenNullUserPassed_ThenNullIsStored() {
        assertDoesNotThrow(() -> {
            userService.addUser(null);
        }, "Should not throw exception when adding null user");
        
        verify(userRepository, times(1)).save(null);
    }

    // Tests for updateUser()
    @Test
    void testUpdateUser_WhenUserExists_ThenUserIsUpdated() {
        User updateData = new User(1L, "UpdatedShambhu", "updated@gmail.com");
        User updatedUser = userService.updateUser(1L, updateData);

        assertNotNull(updatedUser, "Updated user should not be null");
        assertEquals("UpdatedShambhu", updatedUser.getName());
        assertEquals("updated@gmail.com", updatedUser.getEmail());
        
        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    void testUpdateUser_WhenUserDoesNotExist_ThenReturnsNull() {
        User updateData = new User(999L, "NonExistent", "nonexistent@gmail.com");
        User result = userService.updateUser(999L, updateData);

        assertNull(result, "Should return null when user doesn't exist");
        
        verify(userRepository, times(1)).save(updateData);
    }

    @Test
    void testUpdateUser_WhenIdIsNull_ThenReturnsNull() {
        User updateData = new User(1L, "Test", "test@gmail.com");
        User result = userService.updateUser(null, updateData);

        assertNull(result, "Should return null when id is null");
        
        verify(userRepository, times(1)).save(updateData);
    }

    @Test
    void testUpdateUser_WhenUpdateDataIsNull_ThenThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            userService.updateUser(1L, null);
        }, "Should throw NullPointerException when update data is null");
        
        verify(userRepository, times(1)).save(null);
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
        
        verify(userRepository, times(1)).save(afterUpdate);
    }

    @Test
    void testUpdateUser_WhenUpdateWithEmptyStrings_ThenUpdatedWithEmpty() {
        User updateData = new User(1L, "", "");
        User result = userService.updateUser(1L, updateData);

        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals("", result.getEmail());
        
        verify(userRepository, times(1)).save(result);
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
        
        verify(userRepository, times(1)).deleteById(userIdToDelete);
    }

    @Test
    void testDeleteUser_WhenUserDoesNotExist_ThenReturnsFalse() {
        boolean deleted = userService.deleteUser(999L);
        assertFalse(deleted, "Should return false when user doesn't exist");
        
        verify(userRepository, times(1)).deleteById(999L);
    }

    @Test
    void testDeleteUser_WhenIdIsNull_ThenReturnsFalse() {
        boolean deleted = userService.deleteUser(null);
        assertFalse(deleted, "Should return false when id is null");
        
        verify(userRepository, times(1)).deleteById(null);
    }

    @Test
    void testDeleteUser_WhenMultipleUsersDeleted_ThenAllAreRemoved() {
        int initialSize = userService.getAllUsers().size();
        
        userService.deleteUser(1L);
        userService.deleteUser(2L);

        List<User> users = userService.getAllUsers();
        assertEquals(initialSize - 2, users.size(), "Should have 2 fewer users");
        
        verify(userRepository, times(1)).deleteById(1L);
        verify(userRepository, times(1)).deleteById(2L);
    }

    @Test
    void testDeleteUser_WhenSameUserDeletedTwice_ThenSecondReturnsFalse() {
        boolean firstDelete = userService.deleteUser(14L);
        boolean secondDelete = userService.deleteUser(14L);

        assertTrue(firstDelete, "First delete should succeed");
        assertFalse(secondDelete, "Second delete should fail");
        
        verify(userRepository, times(2)).deleteById(14L);
    }

    // Tests for searchUsers()
    @Test
    void testSearchUsers_WhenSearchByName_ThenReturnsMatchingUsers() {
        List<User> results = userService.searchUsers("Amit", "");
        assertFalse(results.isEmpty(), "Should find users matching name");
        assertTrue(results.stream().anyMatch(u -> u.getName().contains("Amit")));
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("Amit", "");
    }

    @Test
    void testSearchUsers_WhenSearchByEmail_ThenIgnoresEmailParameter() {
        List<User> results = userService.searchUsers("gmail", "");
        assertFalse(results.isEmpty(), "Should find users with 'gmail' in email via query parameter");
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("gmail", "");
    }

    @Test
    void testSearchUsers_WhenSearchByPartialName_ThenReturnsMatchingUsers() {
        List<User> results = userService.searchUsers("Raj", "");
        assertFalse(results.isEmpty(), "Should find users with partial name match");
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("Raj", "");
    }

    @Test
    void testSearchUsers_WhenSearchByPartialEmail_ThenReturnsMatchingUsers() {
        List<User> results = userService.searchUsers("", "gmail");
        assertFalse(results.isEmpty(), "Should find users with partial email match");
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("", "gmail");
    }

    @Test
    void testSearchUsers_WhenNoMatch_ThenReturnsEmptyList() {
        List<User> results = userService.searchUsers("NonExistentUser", "");
        assertTrue(results.isEmpty(), "Should return empty list when no match");
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("NonExistentUser", "");
    }

    @Test
    void testSearchUsers_WhenSearchIsEmpty_ThenReturnsAllUsers() {
        List<User> results = userService.searchUsers("", "");
        assertFalse(results.isEmpty(), "Should return all users when search query is empty string");
        assertEquals(userService.getAllUsers().size(), results.size());
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("", "");
    }

    @Test
    void testSearchUsers_WhenSearchIsCaseInsensitive_ThenReturnsResults() {
        List<User> resultsLower = userService.searchUsers("amit", "");
        List<User> resultsUpper = userService.searchUsers("AMIT", "");
        List<User> resultsMixed = userService.searchUsers("AmIt", "");

        assertFalse(resultsLower.isEmpty(), "Lowercase search should work");
        assertFalse(resultsUpper.isEmpty(), "Uppercase search should work");
        assertFalse(resultsMixed.isEmpty(), "Mixed case search should work");
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("amit", "");
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("AMIT", "");
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("AmIt", "");
    }

    @Test
    void testSearchUsers_WhenSearchWithNullQuery_ThenThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            userService.searchUsers(null, "");
        }, "Should throw NullPointerException for null query");
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(null, "");
    }

    @Test
    void testSearchUsers_WhenSearchWithNullEmail_ThenIgnoresEmailParameter() {
        List<User> results = userService.searchUsers("Amit", null);
        assertFalse(results.isEmpty(), "Should find results even with null email since email parameter is unused");
        assertTrue(results.stream().anyMatch(u -> u.getName().contains("Amit")));
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("Amit", null);
    }

    @Test
    void testSearchUsers_WhenMultipleMatches_ThenReturnsAllMatches() {
        List<User> results = userService.searchUsers("a", "");
        assertTrue(results.size() > 1, "Should return multiple matches");
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("a", "");
    }

    @Test
    void testSearchUsers_WhenSearchMatchesMultipleFields_ThenReturnsResult() {
        List<User> results = userService.searchUsers("h", "");
        assertFalse(results.isEmpty(), "Should find users matching the search criteria");
        
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("h", "");
    }
}

