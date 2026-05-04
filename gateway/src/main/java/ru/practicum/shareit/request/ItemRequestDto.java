package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemForRequestDto;

import java.time.Instant;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */

@Setter
@Getter
public class ItemRequestDto {
    private Long id;
    @NotBlank
    private String description;
    private Instant created;
    private List<ItemForRequestDto> items;
}


