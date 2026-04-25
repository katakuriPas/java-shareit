package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    public BookingResponseDto createBooking(Long userId, BookingRequestDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(dto.getItemId())
                        .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь сейчас недоступна для бронирования");
        }

        Booking booking = bookingMapper.toBooking(dto);

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setBookingStatus(BookingStatus.WAITING);

        log.info("Бронь itemId = {}, userId = {}, <{}> успешно создана", item.getId(), userId, booking);
        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    public BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Пользователь не является владельцем вещи");
        }

        if (approved) {
            booking.setBookingStatus(BookingStatus.APPROVED);
            log.info("Статус брони {} успешно изменен на APPROVED", booking);
        } else {
            booking.setBookingStatus(BookingStatus.REJECTED);
            log.info("Статус брони {} успешно изменен на REJECTED", booking);
        }

        return bookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }


    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (!booking.getItem().getOwner().getId().equals(userId) &&
                !booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException("Доступ запрещен");
        }

        return bookingMapper.toBookingResponseDto(booking);
    }


    public List<BookingResponseDto> getAllBookingByUser(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
                break;
            case "CURRENT":
                bookings = bookingRepository.findAllByBookerIdAndCurrent(userId, now);
                break;
            case "PAST":
                bookings = bookingRepository.findAllByBookerIdAndPast(userId, now);
                break;
            case "FUTURE":
                bookings = bookingRepository.findAllByBookerIdAndFuture(userId, now);
                break;
            case "WAITING":
                bookings = bookingRepository.findAllByBookerIdAndBookingStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findAllByBookerIdAndBookingStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                throw new ValidationException("Неизвестный статус: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }


    public List<BookingResponseDto> getAllBookingByOwnerId(Long ownerId, String state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> bookingsByOwner;
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                bookingsByOwner = bookingRepository.findAllByBookerIdOrderByStartDesc(ownerId);
                break;
            case "CURRENT":
                bookingsByOwner = bookingRepository.findAllByBookerIdAndCurrent(ownerId, now);
                break;
            case "PAST":
                bookingsByOwner = bookingRepository.findAllByBookerIdAndPast(ownerId, now);
                break;
            case "FUTURE":
                bookingsByOwner = bookingRepository.findAllByBookerIdAndFuture(ownerId, now);
                break;
            case "WAITING":
                bookingsByOwner = bookingRepository.findAllByBookerIdAndBookingStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
                break;
            case "REJECTED":
                bookingsByOwner = bookingRepository.findAllByBookerIdAndBookingStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
                break;
            default:
                throw new ValidationException("Неизвестный статус: " + state);
        }

        return bookingsByOwner.stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }
}
