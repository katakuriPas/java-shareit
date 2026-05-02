package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestBody BookingRequestDto bookingRequestDto) {

        log.info("PostMapping: createBooking userId = {}, bookingDto = {}", userId, bookingRequestDto);
        return bookingService.createBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @PathVariable Long bookingId,
                                            @RequestParam(name = "approved") Boolean approved) {

        log.info("PatchMapping: approveBooking userId = {}, bookingId = {}, approved = {}",
                userId, bookingId, approved);
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @PathVariable Long bookingId) {

        log.info("GetMapping: getBooking userId = {}, bookingId = {}", userId, bookingId);
        return bookingService.getBooking(userId, bookingId);

    }

    @GetMapping
    public List<BookingResponseDto> getAllByBooker(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {

        log.info("GET /bookings?state={}, userId={}", state, userId);
        return bookingService.getAllBookingByUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "ALL") String state) {

        log.info("GET /owner?state={}, ownerId={}", state, ownerId);
        return bookingService.getAllBookingByOwnerId(ownerId, state);
    }
}
