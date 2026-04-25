package ru.practicum.shareit.user;

import lombok.*;
import ru.practicum.shareit.item.Item;
import jakarta.persistence.*;
import ru.practicum.shareit.item.comment.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Item> items;

    @OneToMany(mappedBy = "author")
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();
}
