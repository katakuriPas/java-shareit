package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("select ir from ItemRequest ir " +
            "where ir.requestion.id = ?1 ORDER BY ir.timeCreated DESC")
    List<ItemRequest> findAllByRequester(Long requesterId);

    @Query("select ir from ItemRequest ir " +
            "where ir.requestion.id != ?1 ORDER BY ir.timeCreated DESC")
    List<ItemRequest> findAllByRequesterOtherUsers(Long userId);
}

