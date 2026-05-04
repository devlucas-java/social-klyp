package com.github.devlucasjava.socialklyp.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "links")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private boolean isActive;

    // chat ao qual este link pertence
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    // quem gerou o link
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_profile_id", nullable = false)
    private Profile profile;

    private long usersSubscribed;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime expirationDate;

    public boolean isExpired() {
        return expirationDate != null && LocalDateTime.now().isAfter(expirationDate);
    }
}
