package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.InMemoryItemStorage;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.InMemoryUserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {
    private static final String ITEM_NOT_FOUND = "Вещь с id =%d не найдена";
    private static final String USER_NOT_FOUND = "Пользователь с id=%d не найден";

    private final InMemoryItemStorage itemStorage;
    private final InMemoryUserStorage userStorage;
    private final UserService userService;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    public ItemDto createItem(Long ownerId, ItemDto itemDto) {
        if (!userStorage.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с id = " + ownerId + " не найден");
        }

        validationItem(itemDto);

        Item item = itemMapper.toItemEntity(itemDto);
        log.info("Создана вещь id={} владельцем id={}", item.getId(), ownerId);
        return itemMapper.toItemDto(itemStorage.createItem(ownerId, item));
    }

    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto newItemDto) {
        Item existingItem = itemMapper.toItemEntity(getItemById(itemId));
        User owner = userMapper.toUserEntity(userService.getUserById(existingItem.getOwnerId()));

        if (!Objects.equals(ownerId, owner.getId())) {
            throw new NotFoundException("Пользователь с id = " + ownerId + " не является владельцем данной вещи");
        }

        Item newItem = itemMapper.toItemEntity(newItemDto);

        log.info("Вещь id={} обновлена", newItem.getId());
        return itemMapper.toItemDto(itemStorage.updateItem(itemId, newItem));
    }

    public ItemDto getItemById(Long itemId) {
        Item existingItem = itemStorage.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND.formatted(itemId)));

        log.info("Вещь id={} получена", existingItem.getId());
        return itemMapper.toItemDto(existingItem);
    }

    public Collection<ItemDto> getAllItemByOwner(Long ownerId) {
        if (userStorage.getUserById(ownerId).isEmpty()) {
            throw new NotFoundException(USER_NOT_FOUND.formatted(ownerId));
        }

        Collection<ItemDto> resItems = itemStorage.getAllItemByOwner(ownerId).stream()
                .map(itemMapper::toItemDto)
                .toList();

        log.info("Получен список вещей {} пользователя id={} получена", resItems, ownerId);
        return resItems;
    }

    public List<ItemDto> findItemByText(Long userId, String text) {

        log.info("Получен список вещей по тексту {}", text);
        return itemStorage.findItemByText(userId, text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    public void validationItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.warn("Ошибка валидации: Название вещи не указано");
            throw new ValidationException("Название вещи должно быть указано");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.warn("Ошибка валидации: Описание не указано ");
            throw new ValidationException("Описание должно быть указан");
        }
        if (itemDto.getAvailable() == null) {
            log.warn("Ошибка валидации: Статус аренды не указан ");
            throw new ValidationException("Статус аренды должен быть указан");
        }
    }
}
