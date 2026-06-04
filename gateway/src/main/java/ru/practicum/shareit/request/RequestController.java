package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TODO Sprint add-item-requests.
 */
@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("@PostMapping: createItemRequest userId = {}, itemRequestDto = {}", userId, itemRequestDto);
        return requestClient.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("@GetMapping: getItemRequest userId = {}", userId);
        return requestClient.getMyRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getItemRequestOtherUsers(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("@GetMapping(\"/all\"): getItemRequestOtherUsers userId = {}", userId);
        return requestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable Long requestId) {
        log.info("@GetMapping(\"/{requestId}\"): getItemRequestById userId = {}, requestId = {}", userId, requestId);
        return requestClient.getRequestById(userId, requestId);
    }
}
