package ru.practicum.shareit.serviceTest;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.RequestService;
import ru.practicum.shareit.user.User;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceIntegrationTest {

    private final RequestService itemRequestService;
    private final EntityManager em;

    @Test
    void getItemRequestById_ShouldReturnRequestWithItems() {
        User requester = new User();
        requester.setName("Pavel");
        requester.setEmail("pavel@mail.com");
        em.persist(requester);

        User owner = new User();
        owner.setName("Ivan");
        owner.setEmail("ivan@mail.com");
        em.persist(owner);

        ItemRequest request = new ItemRequest();
        request.setDescription("Нужна стремянка");
        request.setRequestion(requester);
        request.setTimeCreated(Instant.now());
        em.persist(request);

        Item item = new Item();
        item.setName("Стремянка алюминиевая");
        item.setDescription("3 метра");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setItemRequest(request);
        em.persist(item);

        em.flush();
        em.clear();

        ItemRequestDto result = itemRequestService.getItemRequestById(requester.getId(), request.getId());

        assertThat(result.getId(), equalTo(request.getId()));
        assertThat(result.getDescription(), equalTo("Нужна стремянка"));

        assertThat(result.getItems(), hasSize(1));
        assertThat(result.getItems().get(0).getName(), equalTo("Стремянка алюминиевая"));
    }

    @Test
    void createItemRequest_withBlankDescription_shouldThrowValidationException() {
        User user = new User();
        user.setName("Pavel");
        user.setEmail("pavel@mail.com");
        em.persist(user);
        em.flush();

        ItemRequestDto badDto = ItemRequestDto.builder()
                .description("  ")
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(ru.practicum.shareit.exception.ValidationException.class,
                () -> itemRequestService.createItemRequest(user.getId(), badDto));
    }


    @Test
    void createItemRequest_ShouldSaveRequest() {
        User requester = createAndPersistUser("Requester", "req@mail.com");
        ItemRequestDto inputDto = ItemRequestDto.builder()
                .description("Description")
                .build();

        ItemRequestDto result = itemRequestService.createItemRequest(requester.getId(), inputDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo("Description"));
        assertThat(result.getCreated(), notNullValue());
    }

    @Test
    void getMyItemRequest_ShouldReturnUserRequests() {
        User requester = createAndPersistUser("Requester", "req@mail.com");
        createAndPersistRequest(requester, "Request 1");

        var result = itemRequestService.getMyItemRequest(requester.getId());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getDescription(), equalTo("Request 1"));
    }

    @Test
    void getItemRequestOtherUsers_ShouldReturnOtherUsersRequests() {
        User requester = createAndPersistUser("Requester", "req@mail.com");
        User otherUser = createAndPersistUser("Other", "other@mail.com");
        createAndPersistRequest(otherUser, "Other Request");

        var result = itemRequestService.getItemRequestOtherUsers(requester.getId());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getDescription(), equalTo("Other Request"));
    }

    @Test
    void getItemRequestById_WithInvalidId_ShouldThrowNotFound() {
        User user = createAndPersistUser("User", "u@mail.com");

        org.junit.jupiter.api.Assertions.assertThrows(ru.practicum.shareit.exception.NotFoundException.class,
                () -> itemRequestService.getItemRequestById(user.getId(), 999L));
    }

    private User createAndPersistUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        em.persist(user);
        return user;
    }

    private void createAndPersistRequest(User requester, String desc) {
        ItemRequest request = new ItemRequest();
        request.setDescription(desc);
        request.setRequestion(requester);
        request.setTimeCreated(Instant.now());
        em.persist(request);
        em.flush();
    }

}