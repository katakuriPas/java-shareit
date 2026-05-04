package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    @Mapping(target = "created", source = "timeCreated")
    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "timeCreated", source = "created")
    ItemRequest toItemRequest(ItemRequestDto itemRequestDto);
}
