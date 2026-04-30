package com.github.devlucasjava.socialklyp.infrastructure.database.repository;

import com.github.devlucasjava.socialklyp.domain.entity.Follow;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    Optional<Follow> findByFollowerAndFollowing(Profile follower, Profile following);

    boolean existsByFollowerAndFollowing(Profile follower, Profile following);

    @Query("SELECT f FROM Follow f WHERE f.following = :profile")
    Page<Follow> findFollowersByProfile(@Param("profile") Profile profile, Pageable pageable);

    @Query("SELECT f FROM Follow f WHERE f.follower = :profile")
    Page<Follow> findFollowingByProfile(@Param("profile") Profile profile, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following = :profile")
    long countFollowersByProfile(@Param("profile") Profile profile);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower = :profile")
    long countFollowingByProfile(@Param("profile") Profile profile);
}
