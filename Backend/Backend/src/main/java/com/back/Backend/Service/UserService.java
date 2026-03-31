package com.back.Backend.Service;

import com.back.Backend.Model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private List<User> users = new ArrayList<>();

    public UserService() {
        // Initialize with dummy users
        users.add(new User(1L, "Shambhu", "shambhu@test.com"));
        users.add(new User(2L, "Rahul", "rahul@gmail.com"));
        users.add(new User(3L, "Suresh", "Suresh@gmail.com"));
        users.add(new User(4L, "Ramesh", "Ramesh@gmail.com"));
        users.add(new User(5L, "Amit", "amit@gmail.com"));
        users.add(new User(6L, "Priya", "priya@gmail.com"));
        users.add(new User(7L, "Vikram", "vikram@gmail.com"));
        users.add(new User(8L, "Neha", "neha@gmail.com"));
        users.add(new User(9L, "Rajesh", "rajesh@gmail.com"));
        users.add(new User(10L, "Anjali", "anjali@gmail.com"));
        users.add(new User(11L, "Sunil", "sunil@gmail.com"));
        users.add(new User(12L, "Pooja", "pooja@test.com"));
        users.add(new User(13L, "Manish", "manish@gmail.com"));
        users.add(new User(14L, "Kavita", "kavita@gmail.com"));
        users.add(new User(15L, "Sanjay", "sanjay@gmail.com"));
        users.add(new User(16L, "Ritu", "Ritu@yaho.com"));


    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public User getUserById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public User addUser(User user) {
        users.add(user);
        return user;
    }

    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        if (existingUser != null) {
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
        }
        return existingUser;
    }

    public boolean deleteUser(Long id) {
        return users.removeIf(user -> user.getId().equals(id));
    }

    public List<User> searchUsers(String query, String email) {
        String lowerCaseQuery = query.toLowerCase();
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (user.getName().toLowerCase().contains(lowerCaseQuery) ||
                user.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                result.add(user);
            }
        }
        return result;
    }
}

