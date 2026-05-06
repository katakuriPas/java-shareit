package ru.practicum.shareit.serviceTest;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final EntityManager em;

    @Test
    void getAllBookingByOwnerId_ShouldFilterByState() {
        User owner = createAndPersistUser("Owner", "owner@mail.com");
        User booker = createAndPersistUser("Booker", "booker@mail.com");
        Item item = createAndPersistItem("Отвертка", owner);

        LocalDateTime now = LocalDateTime.now();

        createAndPersistBooking(item, booker, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);
        createAndPersistBooking(item, booker, now.minusDays(1), now.plusDays(1), BookingStatus.APPROVED);
        createAndPersistBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);

        em.flush();
        em.clear();


        List<BookingResponseDto> pastBookings = bookingService.getAllBookingByOwnerId(booker.getId(), "PAST");
        assertThat(pastBookings, hasSize(1));
        assertThat(pastBookings.get(0).getEnd(), lessThan(now));

        List<BookingResponseDto> futureBookings = bookingService.getAllBookingByOwnerId(booker.getId(), "FUTURE");
        assertThat(futureBookings, hasSize(1));
        assertThat(futureBookings.get(0).getStart(), greaterThan(now));

        List<BookingResponseDto> currentBookings = bookingService.getAllBookingByOwnerId(booker.getId(), "CURRENT");
        assertThat(currentBookings, hasSize(1));
        assertThat(currentBookings.get(0).getStart(), lessThan(now));
        assertThat(currentBookings.get(0).getEnd(), greaterThan(now));
    }

    @Test
    void approveBooking_WhenNotOwner_ShouldThrowException() {
        User owner = createAndPersistUser("Owner4", "owner4@mail.com");
        User booker = createAndPersistUser("Booker4", "booker4@mail.com");
        User stranger = createAndPersistUser("Stranger", "stranger@mail.com");
        Item item = createAndPersistItem("Пила", owner);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setBookingStatus(BookingStatus.WAITING);
        em.persist(booking);
        em.flush();

        // Попытка одобрить от лица чужого пользователя (не владельца)
        assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(stranger.getId(), booking.getId(), true)
        );
    }

    @Test
    void approveBooking_WhenRejected_ShouldSetStatusRejected() {
        User owner = createAndPersistUser("Owner5", "owner5@mail.com");
        User booker = createAndPersistUser("Booker5", "booker5@mail.com");
        Item item = createAndPersistItem("Топор", owner);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setBookingStatus(BookingStatus.WAITING);
        em.persist(booking);
        em.flush();

        // Отклоняем бронирование (approved = false)
        BookingResponseDto result = bookingService.approveBooking(owner.getId(), booking.getId(), false);

        assertThat(result.getStatus(), equalTo("REJECTED"));
    }

    @Test
    void getBooking_WhenStranger_ShouldThrowNotFound() {
        User owner = createAndPersistUser("Owner6", "owner6@mail.com");
        User booker = createAndPersistUser("Booker6", "booker6@mail.com");
        User stranger = createAndPersistUser("Stranger2", "stranger2@mail.com");
        Item item = createAndPersistItem("Рулетка", owner);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setBookingStatus(BookingStatus.WAITING);
        em.persist(booking);
        em.flush();

        assertThrows(NotFoundException.class, () ->
                bookingService.getBooking(stranger.getId(), booking.getId())
        );
    }


    @Test
    void getAllBookingByUser_ShouldFilterByAllStates() {
        User owner = createAndPersistUser("Owner3", "owner3@mail.com");
        User booker = createAndPersistUser("Booker3", "booker3@mail.com");
        Item item = createAndPersistItem("Лестница", owner);

        LocalDateTime now = LocalDateTime.now();

        createAndPersistBooking(item, booker, now.minusDays(5), now.minusDays(4), BookingStatus.APPROVED); // PAST
        createAndPersistBooking(item, booker, now.minusDays(1), now.plusDays(1), BookingStatus.APPROVED); // CURRENT
        createAndPersistBooking(item, booker, now.plusDays(2), now.plusDays(3), BookingStatus.WAITING);  // FUTURE / WAITING
        createAndPersistBooking(item, booker, now.plusDays(4), now.plusDays(5), BookingStatus.REJECTED); // FUTURE / REJECTED

        em.flush();
        em.clear();

        // 1. Тест ALL
        assertThat(bookingService.getAllBookingByUser(booker.getId(), "ALL"), hasSize(4));

        // 2. Тест PAST
        assertThat(bookingService.getAllBookingByUser(booker.getId(), "PAST"), hasSize(1));

        // 3. Тест CURRENT
        assertThat(bookingService.getAllBookingByUser(booker.getId(), "CURRENT"), hasSize(1));

        // 4. Тест FUTURE
        assertThat(bookingService.getAllBookingByUser(booker.getId(), "FUTURE"), hasSize(2));

        // 5. Тест WAITING
        assertThat(bookingService.getAllBookingByUser(booker.getId(), "WAITING"), hasSize(1));

        // 6. Тест REJECTED
        assertThat(bookingService.getAllBookingByUser(booker.getId(), "REJECTED"), hasSize(1));
    }

    @Test
    void getAllBookingByUser_WithUnknownUser_ShouldThrowNotFound() {
        assertThrows(NotFoundException.class, () ->
                bookingService.getAllBookingByUser(999L, "ALL")
        );
    }


    @Test
    void createBooking_ShouldSaveAndReturnDto() {
        User booker = createAndPersistUser("BookerNew", "bnew@mail.com");
        User owner = createAndPersistUser("OwnerNew", "onew@mail.com");
        Item item = createAndPersistItem("Дрель", owner);

        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto result = bookingService.createBooking(booker.getId(), dto);

        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
    }

    @Test
    void approveBooking_ShouldUpdateStatus() {
        User owner = createAndPersistUser("OwnerApp", "oapp@mail.com");
        User booker = createAndPersistUser("BookerApp", "bapp@mail.com");
        Item item = createAndPersistItem("Пила", owner);

        // Создаем бронь вручную через EM со статусом WAITING
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setBookingStatus(BookingStatus.WAITING);
        em.persist(booking);
        em.flush();

        BookingResponseDto result = bookingService.approveBooking(owner.getId(), booking.getId(), true);

        assertThat(result.getStatus(), equalTo("APPROVED"));
    }

    @Test
    void getBooking_ShouldReturnDetails_ForOwnerOrBooker() {
        User owner = createAndPersistUser("OwnerGet", "oget@mail.com");
        User booker = createAndPersistUser("BookerGet", "bget@mail.com");
        Item item = createAndPersistItem("Стремянка", owner);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setBookingStatus(BookingStatus.APPROVED);
        em.persist(booking);
        em.flush();

        BookingResponseDto result = bookingService.getBooking(booker.getId(), booking.getId());
        assertThat(result.getId(), equalTo(booking.getId()));
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowException() {
        User owner = createAndPersistUser("OwnerEx", "oex@mail.com");
        User booker = createAndPersistUser("BookerEx", "bex@mail.com");
        Item item = createAndPersistItem("Сломанная вещь", owner);
        item.setAvailable(false); // Делаем вещь недоступной
        em.merge(item);
        em.flush();

        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(item.getId());

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(booker.getId(), dto)
        );
    }


    @Test
    void getAllBookingByOwnerId_StatusFiltering() {
        User owner = createAndPersistUser("Owner2", "owner2@mail.com");
        User booker = createAndPersistUser("Booker2", "booker2@mail.com");
        Item item = createAndPersistItem("Молоток", owner);

        createAndPersistBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        createAndPersistBooking(item, booker, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), BookingStatus.REJECTED);

        em.flush();
        em.clear();

        List<BookingResponseDto> waiting = bookingService.getAllBookingByOwnerId(booker.getId(), "WAITING");
        assertThat(waiting, hasSize(1));
        assertThat(waiting.get(0).getStatus(), equalTo("WAITING"));

        List<BookingResponseDto> rejected = bookingService.getAllBookingByOwnerId(booker.getId(), "REJECTED");
        assertThat(rejected, hasSize(1));
        assertThat(rejected.get(0).getStatus(), equalTo("REJECTED"));
    }

    @Test
    void getAllBookingByOwnerId_UnknownState_ShouldThrowException() {
        User owner = createAndPersistUser("User", "user@mail.com");
        em.persist(owner);

        assertThrows(ValidationException.class, () ->
                bookingService.getAllBookingByOwnerId(owner.getId(), "UNSUPPORTED")
        );
    }

    private User createAndPersistUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        em.persist(user);
        return user;
    }

    private Item createAndPersistItem(String name, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("Описание");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);
        return item;
    }

    private void createAndPersistBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setBookingStatus(status);
        em.persist(booking);
    }
}