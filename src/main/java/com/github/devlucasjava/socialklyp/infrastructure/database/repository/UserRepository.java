package com.github.devlucasjava.socialklyp.infrastructure.database.repository;

import com.github.devlucasjava.socialklyp.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
                SELECT u 
                FROM User u 
                WHERE (u.username = :login OR u.email = :login)
                AND u.deletedAt IS NULL
            """)
    Optional<User> findByUsernameOrEmail(@Param("login") String login);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
