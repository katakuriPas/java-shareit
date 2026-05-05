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
}