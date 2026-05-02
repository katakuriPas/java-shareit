package ru.practicum.shareit.request;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.practicum.shareit.user.User;

import java.time.Instant;

/**
 * TODO Sprint add-item-requests.
 */

@Entity
@Table(name = "item_requests")
@Data
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User requestion;

    @Column(name = "time_created", nullable = false)
    private Instant timeCreated;
}
