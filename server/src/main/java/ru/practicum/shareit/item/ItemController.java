package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemDto itemDto) {

        log.info("PostMapping: createItem userId = {}, iterm = {}", userId, itemDto);
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto) {

        log.info("PatchMapping: update userId = {}, itemId = {}, itemDto = {}",
                userId, itemId, itemDto);
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemWithCommentsDto getItem(@PathVariable Long itemId) {

        log.info("GetMapping(\"/{itemId}\"): getItem itemId = {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public Collection<ItemWithCommentsDto> getAllItemByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {

        log.info("GetMapping: findAllItem ownerId = {}", ownerId);
        return itemService.getAllItemByOwner(ownerId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam String text) {

        log.info("GetMapping(\"/search?text={text}\"): searchItems userId = {}, text = {}", userId, text);
        return itemService.findItemByText(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto) throws BadRequestException {

        log.info("PostMapping: createComment userId = {}, itemId = {}, text = {}", userId, itemId, commentDto);
        return itemService.createComment(userId, itemId, commentDto);
    }
}