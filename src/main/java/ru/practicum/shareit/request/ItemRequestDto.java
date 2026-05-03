package ru.practicum.shareit.request;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemForRequestDto;

import java.time.Instant;
import java.util.*;

/**
 * TODO Sprint add-item-requests.
 */

@Setter
@Getter
public class ItemRequestDto {
    private Long id;
    private String description;
    private Instant created;
    private List<ItemForRequestDto> items = new ArrayList<>();
}
