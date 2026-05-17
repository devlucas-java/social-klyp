package com.github.devlucasjava.socialklyp.infrastructure.database.repository;

import com.github.devlucasjava.socialklyp.domain.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LinkRepository extends JpaRepository<Link, UUID> {

    @Query("SELECT l FROM Link l WHERE l.chat.id = :chatId AND l.isActive = true")
    List<Link> findActiveLinksByChatId(@Param("chatId") UUID chatId);
}
