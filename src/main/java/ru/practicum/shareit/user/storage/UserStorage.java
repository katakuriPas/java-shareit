package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    Collection<User> findAllUsers();

    User createUser(User user);

    User updateUser(Long userId, User newUser);

    void deleteUser(Long id);

    boolean existsByEmail(String email);

    boolean existsById(Long id);

    Optional<User> getUserById (Long id);
}
