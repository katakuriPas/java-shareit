package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ALL
    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    // CURRENT: уже началось, но еще не закончилось
    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 " +
            "AND b.start <= ?2 AND b.end >= ?2 ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndCurrent(Long bookerId, LocalDateTime now);

    // PAST: уже закончилось
    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 " +
            "AND b.end < ?2 ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndPast(Long bookerId, LocalDateTime now);

    // FUTURE: еще не началось
    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 " +
            "AND b.start > ?2 ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndFuture(Long bookerId, LocalDateTime now);

    // ALL
    @Query("SELECT b FROM Booking b JOIN FETCH b.item i WHERE i.owner.id = ?1 ORDER BY b.start DESC")
    List<Booking> findAllByOwnerId(Long ownerId);

    // CURRENT: уже началось, но еще не закончилось
    @Query("SELECT b FROM Booking b JOIN FETCH b.item i WHERE i.owner.id = ?1" +
            "AND b.start <= ?2 AND b.end >= ?2 ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndCurrent(Long ownerId, LocalDateTime now);

    // PAST: уже закончилось
    @Query("SELECT b FROM Booking b JOIN FETCH b.item i WHERE i.owner.id = ?1 " +
            "AND b.end < ?2 ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndPast(Long ownerId, LocalDateTime now);

    // FUTURE: еще не началось
    @Query("SELECT b FROM Booking b JOIN FETCH b.item i WHERE i.owner.id = ?1 " +
            "AND b.start > ?2 ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndFuture(Long ownerId, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE " +
            "b.booker.id = :userId AND " +
            "b.item.id = :itemId AND " +
            "b.bookingStatus = ru.practicum.shareit.booking.BookingStatus.APPROVED AND " +
            "b.end <= :now")
    List<Booking> findAllByItemIdAndBookerIdAndPast(
            @Param("userId") Long userId,
            @Param("itemId") Long itemId,
            @Param("now") java.time.LocalDateTime now);

    @Query("SELECT MAX(b.end) FROM Booking b " +
            "WHERE b.item.id = :itemId AND b.bookingStatus = 'APPROVED' AND b.start <= :now")
    LocalDateTime findLastBookingDate(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT MIN(b.start) FROM Booking b " +
            "WHERE b.item.id = :itemId AND b.bookingStatus = 'APPROVED' AND b.start > :now")
    LocalDateTime findNextBookingDate(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    // Для фильтрации по состоянию (state) в BookingService
    List<Booking> findAllByBookerIdAndBookingStatusOrderByStartDesc(
            Long bookerId, BookingStatus status);

    @Query("SELECT MAX(b.end) FROM Booking b WHERE b.item.id = :itemId")
    LocalDateTime getLatestBookingEnd(@Param("itemId") Long itemId);

    List<Booking> findAllByBookingStatus(BookingStatus status);
}