package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
public class ItemDto {
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long requestId;

    private LocalDateTime lastBooking;

    private LocalDateTime nextBooking;

}
