package com.github.devlucasjava.socialklyp.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "chats")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 3000)
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_profile_id", nullable = false)
    private Profile profileCreator;

    @ManyToMany
    @JoinTable(
            name = "chat_members",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "profile_id")
    )
    private Set<Profile> profiles = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "chat_admins",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "profile_id")
    )
    private Set<Profile> profilesAdmin = new HashSet<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Link> links = new HashSet<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Message> messages = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static final int MAX_MEMBERS = 50;

    public void addMemberProfile(Profile profile) {
        if (profile == null) return;
        profiles.add(profile);
    }

    public void addAdminProfile(Profile profile) {
        if (profile == null) return;
        profilesAdmin.add(profile);
    }


    public int memberCount() {
        java.util.Set<UUID> ids = new java.util.HashSet<>();
        if (profileCreator != null) ids.add(profileCreator.getId());
        if (profiles != null)       profiles.forEach(p -> ids.add(p.getId()));
        if (profilesAdmin != null)  profilesAdmin.forEach(p -> ids.add(p.getId()));
        return ids.size();
    }

    public boolean isFull() {
        return memberCount() >= MAX_MEMBERS;
    }

    public boolean isMember(Profile profile) {
        if (profile == null) return false;
        return (profiles != null && profiles.contains(profile)) ||
               (profilesAdmin != null && profilesAdmin.contains(profile)) ||
               (profileCreator != null && profileCreator.getId().equals(profile.getId()));
    }

    public boolean isAdmin(Profile profile) {
        if (profile == null) return false;
        return (profilesAdmin != null && profilesAdmin.contains(profile)) ||
               (profileCreator != null && profileCreator.getId().equals(profile.getId()));
    }

    public boolean isCreator(Profile profile) {
        if (profile == null) {
            return false;
        }
        return profileCreator != null && Objects.equals(profileCreator.getId(), profile.getId());
    }

    public boolean canAddProfile(Profile profile) {
        return profile != null && !isFull() && !isMember(profile);
    }

    public boolean canRemoveMember(Profile profile) {
        return profile != null && isMember(profile) && !isCreator(profile);
    }

    public boolean canPromoteToAdmin(Profile profile) {
        return profile != null && isMember(profile) && !isAdmin(profile);
    }

    public void removeMemberProfile(Profile profile) {
        if (profile == null) {
            return;
        }
        if (profiles != null) {
            profiles.remove(profile);
        }
        if (profilesAdmin != null) {
            profilesAdmin.remove(profile);
        }
    }

    public void promoteAdminProfile(Profile profile) {
        if (profile == null) {
            return;
        }
        if (isMember(profile)) {
            profilesAdmin.add(profile);
        }
    }
}
