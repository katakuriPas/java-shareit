package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

@Mapper(componentModel = "spring",  uses = {ItemMapper.class, UserMapper.class})
public interface BookingMapper {

    // Превращаем Entity в ответ для Postman
    @Mapping(target = "status", source = "bookingStatus") // Конвертирует Enum в String статус
    @Mapping(target = "booker.id", source = "booker.id")  // Берет ID юзера и кладет в BookerDto.id
    BookingResponseDto toBookingResponseDto(Booking booking);

    // Превращаем входящий запрос в Entity (базовые поля)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "bookingStatus", ignore = true)
    Booking toBooking(BookingRequestDto dto);
}
