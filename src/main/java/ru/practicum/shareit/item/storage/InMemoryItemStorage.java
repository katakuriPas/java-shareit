package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item createItem(Long id, Item item) {
        item.setId(getNextId());
        item.setOwnerId(id);

        items.put(item.getId(), item);
        log.info("Вещь {} успешно создана", item.getName());
        return item;
    }

    @Override
    public Item updateItem(Long itemId, Item newItem) {
        items.put(itemId, newItem);
        log.info("Вещь {} успешно обновлена", newItem.getName());
        return newItem;
    }

    @Override
    public Optional<Item> getItemById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Collection<Item> getAllItemByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findItemByText(Long userId, String text) {
        String lowerText = text.toLowerCase();

        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        List<Item> searchItems = items.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> {
                    String name = item.getName() != null ? item.getName().toLowerCase() : "";
                    String description = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
                    return name.contains(lowerText) || description.contains(lowerText);
                })
                .toList();

        log.debug("Найдено {} вещей по запросу '{}'", searchItems.size(), text);
        log.debug("Список искомых вещей {}", searchItems);
        return searchItems;
    }

    private long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
