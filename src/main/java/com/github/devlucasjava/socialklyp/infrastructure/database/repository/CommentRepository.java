package com.github.devlucasjava.socialklyp.infrastructure.database.repository;

import com.github.devlucasjava.socialklyp.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findAllByPostId(UUID postId, Pageable pageable);
}
