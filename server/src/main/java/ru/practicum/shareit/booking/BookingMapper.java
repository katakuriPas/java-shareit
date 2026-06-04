package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

@Mapper(componentModel = "spring",  uses = {ItemMapper.class, UserMapper.class})
public interface BookingMapper {

    @Mapping(target = "status", source = "bookingStatus")
    @Mapping(target = "booker.id", source = "booker.id")
    BookingResponseDto toBookingResponseDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "bookingStatus", ignore = true)
    Booking toBooking(BookingRequestDto dto);
}
