package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.InMemoryUserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final InMemoryUserStorage userStorage;

    public Collection<User> findAllUser() {
        log.info("Запрос на получение всех пользователей. Количество: {}", userStorage.findAllUsers().size());
        return userStorage.findAllUsers();
    }

    public User createUser(User user) {
        validateUserJson(user);

        if (userStorage.existsByEmail(user.getEmail())) {
            log.warn("Email '{}' уже используется", user.getEmail());
            throw new DuplicatedDataException("Этот email уже используется");
        }

        return userStorage.createUser(user);
    }

    public User updateUser(Long userId, User newUser) {
        User existingUser = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User с id = " + userId + " не найден"));

        log.info("Редактирование пользователя: {}", userId);

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            existingUser.setName(newUser.getName());
        }

        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            if (!existingUser.getEmail().equals(newUser.getEmail())
                    && userStorage.existsByEmail(newUser.getEmail())) {
                throw new DuplicatedDataException("Этот email уже используется");
            }
            existingUser.setEmail(newUser.getEmail());
        }

        return userStorage.updateUser(userId, existingUser);
    }

    public void deleteUser(Long id) {
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("User с id = " + id + " не найден");
        }
        log.info("Получен запрос на удаление пользователя с id {}", id);
        userStorage.deleteUser(id);
        log.info("Удаление пользователя с id = {}", id);
    }

    private void validateUserJson(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: email не указан или не содержит @");
            throw new ValidationException("Email должен быть указан и содержать @");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Ошибка валидации: имя не указано");
            throw new ValidationException("Имя должно быть указано");
        }
    }

    public Optional<User> getUserById(Long id) {
        return userStorage.getUserById(id);
    }
}
