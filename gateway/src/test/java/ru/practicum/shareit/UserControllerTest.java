package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = ShareItGateway.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("John")
                .email("john.doe@mail.com")
                .build();
    }

    @Test
    void findAllUser() throws Exception {
        when(userClient.getUsers())
                .thenReturn(ResponseEntity.ok(List.of(userDto)));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(userDto.getId()))
                .andExpect(jsonPath("$[0].name").value(userDto.getName()));
    }

    @Test
    void getUserById() throws Exception {
        when(userClient.getUser(1L))
                .thenReturn(ResponseEntity.ok(userDto));

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void createUser() throws Exception {
        UserDto inputDto = UserDto.builder().name("John").email("john.doe@mail.com").build();

        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(userDto));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(inputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void updateUser() throws Exception {
        UserDto updateDto = UserDto.builder().name("Updated Name").build();
        UserDto resultDto = UserDto.builder().id(1L).name("Updated Name").email("john.doe@mail.com").build();

        when(userClient.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(resultDto));

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void deleteUser() throws Exception {
        when(userClient.deleteUser(1L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userClient, times(1)).deleteUser(1L);
    }
}
