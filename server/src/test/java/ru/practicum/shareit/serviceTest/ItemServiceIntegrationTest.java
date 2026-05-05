package ru.practicum.shareit.serviceTest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceIntegrationTest {

    private final BookingRepository bookingRepository;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void shouldReturnUserItems() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        ItemDto item1 = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Дрель")
                .description("ДелатьВрумВрум")
                .available(true)
                .build());

        Collection<ItemWithCommentsDto> items = itemService.getAllItemByOwner(owner.getId());

        assertThat(items, hasSize(1));
        assertThat(items.iterator().next().getName(), equalTo(item1.getName()));
    }

    @Test
    void updateItem_ShouldUpdateFields() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("OwnerUpdate").email("update@mail.com").build());
        ItemDto item = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Старая дрель").description("Старая").available(true).build());

        ItemDto updates = ItemDto.builder().name("Новая дрель").available(false).build();
        ItemDto updated = itemService.updateItem(owner.getId(), item.getId(), updates);

        assertThat(updated.getName(), equalTo("Новая дрель"));
        assertThat(updated.getAvailable(), equalTo(false));
        assertThat(updated.getDescription(), equalTo("Старая"));
    }

    @Test
    void updateItem_WhenNotOwner_ShouldThrowException() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner1").email("o1@mail.com").build());
        UserDto stranger = userService.createUser(UserDto.builder()
                .name("Stranger").email("s@mail.com").build());
        ItemDto item = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Вещь").description("Дор").available(true).build());

        assertThrows(NotFoundException.class, () ->
                itemService.updateItem(stranger.getId(), item.getId(), ItemDto.builder().build())
        );
    }

    @Test
    void findItemByText_ShouldReturnMatchingItems() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Searcher").email("search@mail.com").build());
        itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Шуруповерт").description("Крутит шурупы").available(true).build());

        List<ItemDto> result = itemService.findItemByText(owner.getId(), "Шуруп");

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), equalTo("Шуруповерт"));
    }

    @Test
    void findItemByText_WhenEmpty_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.findItemByText(1L, "");
        assertThat(result, hasSize(0));
    }

    @Test
    void validationItem_ShouldThrowExceptions() {
        ItemDto noName = ItemDto.builder().description("Desc").available(true).build();
        assertThrows(ValidationException.class, () -> itemService.validationItem(noName));

        ItemDto noDesc = ItemDto.builder().name("Name").available(true).build();
        assertThrows(ValidationException.class, () -> itemService.validationItem(noDesc));

        ItemDto noAvailable = ItemDto.builder().name("Name").description("Desc").build();
        assertThrows(ValidationException.class, () -> itemService.validationItem(noAvailable));
    }

    @Test
    void createComment_ShouldSaveComment_WhenUserHasPastBooking() throws BadRequestException, org.apache.coyote.BadRequestException {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner").email("owner@test.com").build());
        UserDto booker = userService.createUser(UserDto.builder()
                .name("Booker").email("booker@test.com").build());
        ItemDto itemDto = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Дрель").description("Мощная").available(true).build());

        ru.practicum.shareit.booking.Booking booking = new ru.practicum.shareit.booking.Booking();
        booking.setItem(ru.practicum.shareit.item.Item.builder().id(itemDto.getId()).build());
        booking.setBooker(ru.practicum.shareit.user.User.builder().id(booker.getId()).build());
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setBookingStatus(ru.practicum.shareit.booking.BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная дрель, выручила!");

        CommentDto savedComment = itemService.createComment(booker.getId(), itemDto.getId(), commentDto);

        assertThat(savedComment.getText(), equalTo(commentDto.getText()));
        assertThat(savedComment.getAuthorName(), equalTo("Booker"));
        assertThat(savedComment.getId(), notNullValue());
    }

    @Test
    void getItemById_whenItemNotFound_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                itemService.getItemById(999L)
        );
    }

    @Test
    void getAllItemByOwner_whenUserNotFound_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                itemService.getAllItemByOwner(999L)
        );
    }

    @Test
    void createComment_ShouldThrowException_WhenNoPastBooking() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner2").email("owner2@test.com").build());
        UserDto stranger = userService.createUser(UserDto.builder()
                .name("Stranger").email("stranger@test.com").build());
        ItemDto itemDto = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Пила").description("Острая").available(true).build());

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Я ее даже не брал, но напишу");

        assertThrows(org.apache.coyote.BadRequestException.class, () ->
                itemService.createComment(stranger.getId(), itemDto.getId(), commentDto)
        );
    }

    @Test
    void getItemById_shouldReturnItemWithComments() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner").email("getItem@mail.com").build());
        ItemDto item = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Вещь").description("Описание").available(true).build());

        ItemWithCommentsDto result = itemService.getItemById(item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo("Вещь"));
        assertThat(result.getComments(), hasSize(0));
    }


    @Test
    void createComment_WhenUserNotFound_ShouldThrowNotFoundException() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test");

        assertThrows(NotFoundException.class, () ->
                itemService.createComment(999L, 1L, commentDto)
        );
    }

    @Test
    void createComment_WhenItemNotFound_ShouldThrowNotFoundException() {
        UserDto user = userService.createUser(UserDto.builder()
                .name("Name").email("unique@mail.com").build());
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test");

        assertThrows(NotFoundException.class, () ->
                itemService.createComment(user.getId(), 999L, commentDto)
        );
    }

    @Test
    void getItemById_WhenNotFound_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                itemService.getItemById(1L)
        );
    }

    @Test
    void updateItem_OnlyDescription_ShouldKeepOldName() {
        UserDto owner = userService.createUser(UserDto.builder()
                .name("Owner").email("desc_update@mail.com").build());
        ItemDto item = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Original Name").description("Old Desc").available(true).build());

        ItemDto updates = ItemDto.builder().description("New Desc").build();
        ItemDto updated = itemService.updateItem(owner.getId(), item.getId(), updates);

        assertThat(updated.getDescription(), equalTo("New Desc"));
        assertThat(updated.getName(), equalTo("Original Name"));
        assertThat(updated.getAvailable(), equalTo(true));
    }


}