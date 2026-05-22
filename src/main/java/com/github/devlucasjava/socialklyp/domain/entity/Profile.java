package com.github.devlucasjava.socialklyp.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String bio;

    private String profilePictureUrl;

    private boolean isPrivate = false;

    @OneToOne(mappedBy = "profile")
    private User user;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> following;

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> followers;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Link> links;

    public boolean isSameAs(Profile other) {
        return other != null && Objects.equals(this.id, other.id);
    }

    public boolean isFollowing(Profile target) {
        if (target == null || following == null) {
            return false;
        }
        return following.stream()
                .map(Follow::getFollowing)
                .anyMatch(target::isSameAs);
    }

    public boolean isFollowedBy(Profile source) {
        if (source == null || followers == null) {
            return false;
        }
        return followers.stream()
                .map(Follow::getFollower)
                .anyMatch(source::isSameAs);
    }

    public boolean canFollow(Profile target) {
        return target != null && !isSameAs(target) && !isFollowing(target);
    }
}
