package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage{

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAllUsers() {
        return users.values();
    }

    @Override
    public User createUser(User user) {
        log.info("Попытка создания пользователя: {}", user.getName());

        //validateUser(user);

        user.setId(getNextId());
        log.info("Пользователь успешно добавлен: {}", user.getName());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(Long userId, User newUser) {

        users.put(userId, newUser);
        log.info("Пользователь с id = {} успешно обновлён", userId);
        return newUser;
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }


    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
