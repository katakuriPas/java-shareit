package ru.practicum.shareit.serviceTest;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceIntegrationTest {

    private final UserService userService;
    private final EntityManager em;

    @Test
    void updateUser_shouldUpdateNameAndEmail() {
        User user = new User();
        user.setName("Old Name");
        user.setEmail("old@email.com");
        em.persist(user);
        em.flush();

        UserDto updateDto = UserDto.builder()
                .name("New Name")
                .email("new@email.com")
                .build();

        UserDto result = userService.updateUser(user.getId(), updateDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo("New Name"));
        assertThat(result.getEmail(), equalTo("new@email.com"));

        User savedUser = em.find(User.class, user.getId());
        assertThat(savedUser.getName(), equalTo("New Name"));
        assertThat(savedUser.getEmail(), equalTo("new@email.com"));
    }

    @Test
    void findAllUser_shouldReturnAllUsers() {
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@test.com");
        em.persist(user1);

        var users = userService.findAllUser();

        assertThat(users.isEmpty(), equalTo(false));
    }

    @Test
    void createUser_whenEmailExists_shouldThrowException() {
        User user = new User();
        user.setName("User1");
        user.setEmail("double@test.com");
        em.persist(user);
        em.flush();

        UserDto duplicateDto = UserDto.builder().name("User2").email("double@test.com").build();

        org.junit.jupiter.api.Assertions.assertThrows(ru.practicum.shareit.exception.DuplicatedDataException.class,
                () -> userService.createUser(duplicateDto));
    }

    @Test
    void deleteUser_whenIdNotFound_shouldThrowException() {
        org.junit.jupiter.api.Assertions.assertThrows(ru.practicum.shareit.exception.NotFoundException.class,
                () -> userService.deleteUser(999L));
    }

    @Test
    void createUser_whenNameIsBlank_shouldThrowException() {
        UserDto badUser = UserDto.builder().name("").email("valid@mail.com").build();
        org.junit.jupiter.api.Assertions.assertThrows(ru.practicum.shareit.exception.ValidationException.class,
                () -> userService.createUser(badUser));
    }

    @Test
    void createUser_whenEmailIsInvalid_shouldThrowException() {
        UserDto badUser = UserDto.builder().name("Name").email("invalid-email").build();
        org.junit.jupiter.api.Assertions.assertThrows(ru.practicum.shareit.exception.ValidationException.class,
                () -> userService.createUser(badUser));
    }


    @Test
    void updateUser_whenEmailAlreadyExists_shouldThrowException() {
        User user1 = new User();
        user1.setName("User1");
        user1.setEmail("email1@test.com");
        em.persist(user1);

        User user2 = new User();
        user2.setName("User2");
        user2.setEmail("email2@test.com");
        em.persist(user2);
        em.flush();

        UserDto updateDto = UserDto.builder().email("email2@test.com").build();

        org.junit.jupiter.api.Assertions.assertThrows(ru.practicum.shareit.exception.DuplicatedDataException.class,
                () -> userService.updateUser(user1.getId(), updateDto));
    }


    @Test
    void deleteUser_shouldRemoveUserFromDb() {
        User user = new User();
        user.setName("To Delete");
        user.setEmail("delete@test.com");
        em.persist(user);
        em.flush();

        userService.deleteUser(user.getId());

        User deletedUser = em.find(User.class, user.getId());
        assertThat(deletedUser, equalTo(null));
    }

    @Test
    void getUserDtoById_shouldReturnCorrectDto() {
        User user = new User();
        user.setName("Find Me");
        user.setEmail("find@test.com");
        em.persist(user);
        em.flush();

        UserDto result = userService.getUserDtoById(user.getId());

        assertThat(result.getName(), equalTo("Find Me"));
    }

    // Тесты на исключения (покрывают orElseThrow и if)

    @Test
    void updateUser_withSameEmail_shouldNotThrowError() {
        User user = new User();
        user.setName("User");
        user.setEmail("same@test.com");
        em.persist(user);
        em.flush();

        UserDto updateDto = UserDto.builder().email("same@test.com").build();
        UserDto result = userService.updateUser(user.getId(), updateDto);

        assertThat(result.getEmail(), equalTo("same@test.com"));
    }

    @Test
    void getUserById_invalidId_shouldThrowNotFound() {
        org.junit.jupiter.api.Assertions.assertThrows(ru.practicum.shareit.exception.NotFoundException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    void createUser_withInvalidData_shouldThrowValidationException() {
        UserDto badUser = UserDto.builder().name("").email("no-at-sign").build();

        org.junit.jupiter.api.Assertions.assertThrows(ru.practicum.shareit.exception.ValidationException.class,
                () -> userService.createUser(badUser));
    }
}