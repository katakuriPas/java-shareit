package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestService {

    private final UserService userService;

    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;

    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        User existingUser = userService.getUserById(userId);

        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не должно быть пустым");
        }

        ItemRequest newItemRequest = itemRequestMapper.toItemRequest(itemRequestDto);
        newItemRequest.setRequestion(existingUser);
        newItemRequest.setTimeCreated(Instant.now());

        log.info("ItemRequest.getRequestion: {}", newItemRequest.getRequestion());

        log.info("Сохранение ItemRequest: {}", newItemRequest);
        ItemRequest savedRequest = requestRepository.save(newItemRequest);
        return itemRequestMapper.toItemRequestDto(savedRequest);
    }

    public List<ItemRequestDto> getMyItemRequest(Long userId) {
        userService.getUserById(userId);

        List<ItemRequest> itemRequests = requestRepository.findAllByRequester(userId);

        List<ItemRequestDto> resItemRequestDto = addAnswersToAllRequest(itemRequests);

        log.info("List<ItemRequestDto> getItemRequest(userId = {}): {}", userId, resItemRequestDto);
        return resItemRequestDto;
    }

    public List<ItemRequestDto> getItemRequestOtherUsers(Long userId) {
        userService.getUserById(userId);

        List<ItemRequest> itemRequests = requestRepository.findAllByRequesterOtherUsers(userId);

        List<ItemRequestDto> resItemRequestDto = addAnswersToAllRequest(itemRequests);

        log.info("List<ItemRequestDto> getItemRequestOtherUsers(userId = {}): {}", userId, resItemRequestDto);
        return resItemRequestDto;
    }

    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        userService.getUserById(userId);

        ItemRequest itemRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("ItemRequest с id = " + requestId + " не найден"));

        return addAnswersToRequest(itemRequestMapper.toItemRequestDto(itemRequest));
    }

    private List<ItemRequestDto> addAnswersToAllRequest(List<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(itemRequestMapper::toItemRequestDto)
                .map(this::addAnswersToRequest)
                .toList();
    }

    private ItemRequestDto addAnswersToRequest(ItemRequestDto itemRequestDto) {
        List<ItemForRequestDto> itemForRequestDtos = itemRepository.findAllByItemRequestId(itemRequestDto.getId()).stream()
                .map(itemMapper::toItemForRequestDto)
                .toList();
        itemRequestDto.setItems(itemForRequestDtos);
        return itemRequestDto;
    }
}
