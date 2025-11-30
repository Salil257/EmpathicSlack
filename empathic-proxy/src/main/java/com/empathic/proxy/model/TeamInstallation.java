package com.empathic.proxy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_installations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamInstallation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String teamId;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String botUserId;

    @Column(nullable = false)
    private LocalDateTime installedAt;

    @PrePersist
    protected void onCreate() {
        installedAt = LocalDateTime.now();
    }
}

