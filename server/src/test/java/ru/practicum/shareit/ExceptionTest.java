package ru.practicum.shareit;


import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExceptionTest {

    @Test
    void testExceptions() {
        assertNotNull(new NotFoundException("test").getMessage());
        assertNotNull(new ValidationException("test").getMessage());
    }
}
