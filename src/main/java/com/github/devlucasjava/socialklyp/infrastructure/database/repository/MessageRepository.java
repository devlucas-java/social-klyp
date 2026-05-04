package com.github.devlucasjava.socialklyp.infrastructure.database.repository;

import com.github.devlucasjava.socialklyp.domain.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdAt ASC")
    Page<Message> findByChatId(@Param("chatId") UUID chatId, Pageable pageable);
}
