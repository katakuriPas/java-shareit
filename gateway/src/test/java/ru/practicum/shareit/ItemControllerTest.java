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
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = ShareItGateway.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    private ItemDto itemDto;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);
    }

    @Test
    void createItem() throws Exception {
        when(itemClient.createItem(eq(userId), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(itemDto));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void updateItem() throws Exception {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новая дрель");

        when(itemClient.updateItem(eq(userId), eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(itemDto));

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getItem() throws Exception {
        when(itemClient.getItem(userId, 1L))
                .thenReturn(ResponseEntity.ok(itemDto));

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getAllItemByOwner() throws Exception {
        when(itemClient.getItemsByOwner(userId))
                .thenReturn(ResponseEntity.ok(List.of(itemDto)));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void searchItems() throws Exception {
        when(itemClient.searchItems(eq(userId), anyString()))
                .thenReturn(ResponseEntity.ok(List.of(itemDto)));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void createComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("СУПЕР ПУПЕР ДУПЕР дрель!");
        commentDto.setAuthorName("Ivan");
        commentDto.setCreated(LocalDateTime.now());

        when(itemClient.createComment(eq(userId), eq(1L), any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok(commentDto));

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("СУПЕР ПУПЕР ДУПЕР дрель!"));
    }

    @Test
    void createItem_WhenNameIsBlank() throws Exception {
        ItemDto badDto = new ItemDto();
        badDto.setDescription("Описание без имени");

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(badDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
