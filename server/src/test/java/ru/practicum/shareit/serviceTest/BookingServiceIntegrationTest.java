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
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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