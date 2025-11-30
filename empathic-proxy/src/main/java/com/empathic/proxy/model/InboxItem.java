package com.empathic.proxy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inbox_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboxItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String teamId;

    @Column(nullable = false)
    private String channelId;

    @Column(nullable = false)
    private String messageTs;

    @Column(nullable = false)
    private String threadTs;

    @Column(nullable = false, length = 2000)
    private String messageText;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemType type;

    @Column(length = 500)
    private String action;

    @Column(length = 2000)
    private String suggestedReply;

    @Column(nullable = false)
    private Boolean processed = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ItemType {
        MENTION,
        DM,
        PRIORITY_CHANNEL
    }
}

