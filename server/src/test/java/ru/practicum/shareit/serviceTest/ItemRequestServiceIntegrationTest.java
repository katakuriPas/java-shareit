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
}
