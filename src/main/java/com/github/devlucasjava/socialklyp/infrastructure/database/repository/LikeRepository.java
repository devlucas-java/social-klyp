package com.github.devlucasjava.socialklyp.infrastructure.database.repository;

import com.github.devlucasjava.socialklyp.domain.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {
    boolean existsByProfileIdAndPostId(UUID profileId, UUID postId);

    Optional<Like> findByProfileIdAndPostId(UUID profileId, UUID postId);

    long countByPostId(UUID postId);
}
