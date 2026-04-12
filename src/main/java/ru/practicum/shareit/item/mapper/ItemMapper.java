package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@Component
@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(source = "itemRequest.id", target = "itemRequestId")
    ItemDto toItemDto(Item item);

    Item toItemEntity(ItemDto itemDto);
}