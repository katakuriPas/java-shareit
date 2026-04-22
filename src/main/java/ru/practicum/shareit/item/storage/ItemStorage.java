package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {

    Item createItem(Long id, Item item);

    Item updateItem(Long itemId, Item newItem);

    Optional<Item> getItemById(Long id);

    Collection<Item> getAllItemByOwner(Long ownerId);

    Collection<Item> findItemByText(Long userId, String text);
}
