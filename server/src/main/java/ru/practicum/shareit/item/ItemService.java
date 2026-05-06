package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.RequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {
    private static final String ITEM_NOT_FOUND = "Вещь с id =%d не найдена";
    private static final String USER_NOT_FOUND = "Пользователь с id=%d не найден";

    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RequestRepository requestRepository;

    private final ItemMapper itemMapper;

    public ItemDto createItem(Long ownerId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь c " + ownerId + "  не найден"));


        validationItem(itemDto);

        Item item = itemMapper.toItemEntity(itemDto);
        item.setOwner(owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с id " + itemDto.getRequestId() + " не найден"));
            item.setItemRequest(request);
        } else {
            item.setItemRequest(null);
        }

        log.info("Создана вещь {} id={} владельцем id={}", item, item.getId(), ownerId);
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto updatedItemDto) {
        Item itemToUpdate = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
        User owner = itemToUpdate.getOwner();

        if (!Objects.equals(ownerId, owner.getId())) {
            throw new NotFoundException("Пользователь с id = " + ownerId + " не является владельцем данной вещи");
        }

        if (updatedItemDto.getName() != null) {
            itemToUpdate.setName(updatedItemDto.getName());
        }

        if (updatedItemDto.getDescription() != null) {
            itemToUpdate.setDescription(updatedItemDto.getDescription());
        }

        if (updatedItemDto.getAvailable() != null) {
            itemToUpdate.setAvailable(updatedItemDto.getAvailable());
        }

        ItemDto itemDto = itemMapper.toItemDto(itemRepository.save(itemToUpdate));
        log.info("Вещь с id = {} успешно обновлена", itemId);

        return itemDto;
    }

    public ItemWithCommentsDto getItemById(Long itemId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND.formatted(itemId)));

        List<CommentDto> commentsDto = commentRepository.findAllByItemId(itemId).stream()
                .map(itemMapper::toCommentDto)
                .toList();

        log.info("Вещь id={} получена", existingItem.getId());
        return itemMapper.toItemWithCommentsDto(existingItem, commentsDto);
    }

    public List<ItemWithCommentsDto> getAllItemByOwner(Long ownerId) {
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new NotFoundException(USER_NOT_FOUND.formatted(ownerId));
        }

        LocalDateTime now = LocalDateTime.now();

        log.info("Запрос всех вещей владельца с id = {}", ownerId);
        List<Item> items = itemRepository.findAllByOwnerId(ownerId).stream()
                .map(item -> {
                    LocalDateTime lastBookingDate = bookingRepository.findLastBookingDate(item.getId(), now);
                    LocalDateTime nextBookingDate = bookingRepository.findNextBookingDate(item.getId(), now);

                    item.setLastBooking(lastBookingDate);
                    item.setNextBooking(nextBookingDate);

                    return item;
                })
                .toList();

        List<ItemWithCommentsDto> resItems = items.stream()
                .map(item -> {
                    List<CommentDto> commentsDto = commentRepository.findAllByItemId(item.getId()).stream()
                            .map(itemMapper::toCommentDto)
                            .toList();

                    return itemMapper.toItemWithCommentsDto(item, commentsDto);
                }).toList();


        log.info("Получен список вещей {} пользователя id={} получена", resItems, ownerId);
        return resItems;
    }

    public List<ItemDto> findItemByText(Long userId, String text) {
        if (text == null || text.isBlank()) {
            log.info("Текст запроса пустой");
            return new ArrayList<>();
        }

        log.info("Получен список вещей по тексту {}", text);
        List<Item> searchItems = itemRepository.search(text);

        return searchItems.stream()
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

    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) throws BadRequestException {
        log.info("Запрос от пользователя {} для вещи {}", userId, itemId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        LocalDateTime nowWithBuffer = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findAllByItemIdAndBookerIdAndPast(userId, itemId, nowWithBuffer);


        if (bookings.isEmpty()) {
            log.warn("Отказ в отзыве: У пользователя {} нет завершенных аренд вещи {}", userId, itemId);
            throw new BadRequestException("Вы не можете оставить отзыв: аренда не найдена или еще не завершена");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(nowWithBuffer);

        Comment savedComment = commentRepository.save(comment);

        return new CommentDto(
                savedComment.getId(),
                savedComment.getText(),
                user.getName(),
                savedComment.getCreated()
        );
    }


}
