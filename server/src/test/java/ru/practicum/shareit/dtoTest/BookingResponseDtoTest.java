package ru.practicum.shareit.dtoTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingResponseDtoTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Test
    void testBookingResponseDto() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 10, 10, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 10, 11, 10, 0, 0);

        BookingResponseDto dto = BookingResponseDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status("APPROVED")
                .build();

        JsonContent<BookingResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2024-10-10T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2024-10-11T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }
}

