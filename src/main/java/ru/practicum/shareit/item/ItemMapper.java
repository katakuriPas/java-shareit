package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(source = "itemRequest.id", target = "requestId")
    ItemDto toItemDto(Item item);

    @Mapping(target = "comments", source = "comments")
    ItemWithCommentsDto toItemWithCommentsDto(Item item, List<CommentDto> comments);

    @Mapping(target = "authorName", source = "author.name")
    CommentDto toCommentDto(Comment comment);

    @Mapping(source = "requestId",
            target = "itemRequest.id",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Item toItemEntity(ItemDto itemDto);

    @Mapping(source = "owner.id", target = "ownerId")
    ItemForRequestDto toItemForRequestDto(Item item);
}