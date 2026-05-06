package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.RequestClient;
import ru.practicum.shareit.request.RequestController;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

@WebMvcTest(controllers = RequestController.class)
@ContextConfiguration(classes = ShareItGateway.class)
public class RequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private RequestClient requestClient;

    private ItemRequestDto itemRequestDto;

    private final Long userId = 1L;

    @Test
    void createItemRequest_Standard_ShouldReturnCreated() throws Exception {
        ItemRequestDto inputDto = new ItemRequestDto();
        inputDto.setDescription("Нужна дрель");

        ItemRequestDto outputDto = new ItemRequestDto();
        outputDto.setId(1L);
        outputDto.setDescription("Нужна дрель");
        outputDto.setCreated(Instant.now());

        when(requestClient.createRequest(eq(userId), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(outputDto));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(inputDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void createItemRequest_EmptyDescription_ShouldReturnBadRequest() throws Exception {
        ItemRequestDto invalidDto = new ItemRequestDto();
        invalidDto.setDescription("");

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    void getItemRequest_ShouldReturnList() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1L);

        when(requestClient.getMyRequests(userId))
                .thenReturn(ResponseEntity.ok(List.of(requestDto)));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getItemRequestOtherUsers_ShouldReturnList() throws Exception {
        when(requestClient.getAllRequests(userId))
                .thenReturn(ResponseEntity.ok(List.of()));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void getItemRequestById_ShouldReturnRequest() throws Exception {
        Long requestId = 99L;
        ItemRequestDto outputDto = new ItemRequestDto();
        outputDto.setId(requestId);

        when(requestClient.getRequestById(userId, requestId))
                .thenReturn(ResponseEntity.ok(outputDto));

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));
    }
}
