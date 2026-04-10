package com.back.Backend.Repository;

import com.back.Backend.Model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindAll() {
        List<User> users = userRepository.findAll();
        assertTrue(users.size() >= 16);
    }

    @Test
    void testFindById() {
        Optional<User> user = userRepository.findById(1L);
        assertTrue(user.isPresent());
        assertEquals("Shambhu", user.get().getName());
    }

    @Test
    void testSave() {
        User newUser = new User("NewUser", "new@example.com");
        User saved = userRepository.save(newUser);
        assertNotNull(saved.getId());
        assertEquals("NewUser", saved.getName());
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        List<User> users = userRepository.findByNameContainingIgnoreCase("amit");
        assertFalse(users.isEmpty());
    }

    @Test
    void testFindByEmail() {
        User user = userRepository.findByEmail("rahul@gmail.com");
        assertNotNull(user);
    }

    @Test
    void testFindByNameContainingIgnoreCaseOrEmailContainingIgnoreCase() {
        List<User> users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("gmail", "gmail");
        assertFalse(users.isEmpty());
    }
}
