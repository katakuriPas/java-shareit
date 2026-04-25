package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;

import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(source = "itemRequest.id", target = "itemRequestId")
    ItemDto toItemDto(Item item);

    @Mapping(target = "comments", source = "comments")
    ItemWithCommentsDto toItemWithCommentsDto(Item item, List<CommentDto> comments);

    @Mapping(target = "authorName", source = "author.name")
    CommentDto toCommentDto(Comment comment);

    Item toItemEntity(ItemDto itemDto);
}