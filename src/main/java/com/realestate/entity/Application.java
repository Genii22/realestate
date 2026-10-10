package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    // Заявка может быть анонимной — applicant = null,
    // а контакты лежат в applicantName / applicantPhone.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private User applicant;

    @Column(name = "applicant_name", length = 100)
    private String applicantName;

    @Column(name = "applicant_phone", length = 20)
    private String applicantPhone;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "visit_date")
    private LocalDateTime visitDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getContactName() {
        return applicant != null ? applicant.getFullName() : applicantName;
    }

    public String getContactPhone() {
        return applicant != null ? applicant.getPhone() : applicantPhone;
    }
}
