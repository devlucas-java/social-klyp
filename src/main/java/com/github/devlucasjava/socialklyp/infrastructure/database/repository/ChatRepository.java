package com.github.devlucasjava.socialklyp.infrastructure.database.repository;

import com.github.devlucasjava.socialklyp.domain.entity.Chat;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    @Query("SELECT c FROM Chat c WHERE c.profileCreator = :profile OR :profile MEMBER OF c.profiles OR :profile MEMBER OF c.profilesAdmin")
    Page<Chat> findByMember(@Param("profile") Profile profile, Pageable pageable);
}
