package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Collection<UserDto> findAllUser() {
        log.info("Запрос на получение всех пользователей. Количество: {}", userRepository.findAll().size());
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toUserEntity(userDto);
        validateUser(user);

        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Email '{}' уже используется", user.getEmail());
            throw new DuplicatedDataException("Этот email уже используется");
        }

        return userMapper.toUserDto(userRepository.save(user));
    }

    public UserDto updateUser(Long userId, UserDto newUserDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User с id = " + userId + " не найден"));

        log.info("Редактирование пользователя: {}", userId);

        if (newUserDto.getName() != null && !newUserDto.getName().isBlank()) {
            existingUser.setName(newUserDto.getName());
        }

        if (newUserDto.getEmail() != null && !newUserDto.getEmail().isBlank()) {
            String newEmail = newUserDto.getEmail();

            if (!existingUser.getEmail().equals(newEmail)) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new DuplicatedDataException("Этот email уже используется");
                }
                existingUser.setEmail(newEmail);
            }
        }

        User savedUser = userRepository.save(existingUser);
        return userMapper.toUserDto(savedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User с id = " + id + " не найден");
        }
        log.info("Получен запрос на удаление пользователя с id {}", id);
        userRepository.deleteById(id);
        log.info("Удаление пользователя с id = {}", id);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: email не указан или не содержит @");
            throw new ValidationException("Email должен быть указан и содержать @");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Ошибка валидации: имя не указано");
            throw new ValidationException("Имя должно быть указано");
        }
    }

    public UserDto getUserDtoById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User с id = " + userId + " не найден"));
        return userMapper.toUserDto(user);
    }

    public User getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User с id = " + userId + " не найден"));
        return user;
    }
}
