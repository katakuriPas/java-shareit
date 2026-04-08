package ru.practicum.shareit.user.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */

@Data
public class User {
    private Long id;
    private String name;
    private String email;
}
