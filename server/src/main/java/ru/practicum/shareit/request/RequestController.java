package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody ItemRequestDto itemRequestDto) {
        log.info("@PostMapping: createItemRequest userId = {}, itemRequestDto = {}", userId, itemRequestDto);
        return requestService.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("@GetMapping: getItemRequest userId = {}", userId);
        return requestService.getMyItemRequest(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getItemRequestOtherUsers(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("@GetMapping(\"/all\"): getItemRequestOtherUsers userId = {}", userId);
        return requestService.getItemRequestOtherUsers(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @PathVariable Long requestId) {
        log.info("@GetMapping(\"/{requestId}\"): getItemRequestById userId = {}, requestId = {}", userId, requestId);
        return requestService.getItemRequestById(userId, requestId);
    }
}
