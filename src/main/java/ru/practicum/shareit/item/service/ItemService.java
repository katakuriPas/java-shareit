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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.InMemoryUserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {

    private final InMemoryItemStorage itemStorage;
    private final InMemoryUserStorage userStorage;
    private final ItemMapper itemMapper;

    public ItemDto createItem(Long ownerId, ItemDto itemDto) {
        if (!userStorage.existsById(ownerId)) {
            throw new NotFoundException("User с id = " + ownerId + " не найден");
        }

        validationItem(itemDto);

        Item item = itemMapper.toItemEntity(itemDto);
        return itemMapper.toItemDto(itemStorage.createItem(ownerId, item));
    }

    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto newItemDto) {
        Item existingItem = itemStorage.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с itemId = " + itemId + " не найдена"));
        User owner = userStorage.getUserById(existingItem.getOwnerId())
                .orElseThrow(() -> new NotFoundException("Владелец вещи " + itemId + " отсутствует"));

        if (!Objects.equals(ownerId, owner.getId())) {
            User falseOwner = userStorage.getUserById(ownerId).
                    orElseThrow(() -> new NotFoundException("Пользователь с  ownerId = " + ownerId + " отсутствует"));

            throw new NotFoundException("Пользователь " + falseOwner.getName() + " не является владельцем данной вещи");
        }

        Item newItem = itemMapper.toItemEntity(newItemDto);

        return itemMapper.toItemDto(itemStorage.updateItem(itemId, newItem));
    }

    public ItemDto getItemById(Long itemId) {
        Item existingItem = itemStorage.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с itemId = " + itemId + " не найдена"));
        return itemMapper.toItemDto(existingItem);
    }

    public Collection<ItemDto> getAllItemByOwner(Long ownerId) {
        if (userStorage.getUserById(ownerId).isEmpty()) {
            throw new NotFoundException("Пользователь с ownerId = " + ownerId + " отсутствует");
        }

        return itemStorage.getAllItemByOwner(ownerId).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    public List<ItemDto> findItemByText(Long userId, String text) {

        return itemStorage.findItemByText(userId,text).stream()
                .map(itemMapper :: toItemDto)
                .toList();
    }

    public void validationItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.warn("Ошибка валидации: Имя не указано");
            throw new ValidationException("Name должен быть указан");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.warn("Ошибка валидации: описание не указано ");
            throw new ValidationException("Description должен быть указан");
        }
        if (itemDto.getAvailable() == null || itemDto.getDescription().isBlank()) {
            log.warn("Ошибка валидации: Статус аренды не указан ");
            throw new ValidationException("Available должен быть указан");
        }
    }
}
