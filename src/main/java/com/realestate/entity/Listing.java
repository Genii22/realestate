package com.realestate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @NotBlank
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "deal_type", nullable = false, length = 10)
    private DealType dealType;

    @NotNull
    @Positive
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private ListingStatus status = ListingStatus.ACTIVE;

    @Column(name = "views_count", nullable = false)
    private Integer viewsCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    // ----------- Связи -----------
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    private List<Deal> deals = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    private List<Favorite> favorites = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.expiresAt = LocalDate.now().plusDays(30);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Первичное изображение
    public String getPrimaryImageUrl() {
        return images.stream()
                .filter(ListingImage::getIsPrimary)
                .map(ListingImage::getImageUrl)
                .findFirst()
                .orElse("/img/no-photo.jpg");
    }
}
