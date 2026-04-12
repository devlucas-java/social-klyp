package com.github.devlucasjava.socialklyp.domain.entity;

import com.github.devlucasjava.socialklyp.domain.enuns.MediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime CreatedAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime UpdatedAt;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
