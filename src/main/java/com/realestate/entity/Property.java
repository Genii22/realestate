package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private PropertyType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(name = "street", nullable = false, length = 200)
    private String street;

    @Column(name = "building_number", length = 10)
    private String buildingNumber;

    @Column(name = "apartment_number", length = 10)
    private String apartmentNumber;

    @Column(name = "area", nullable = false, precision = 10, scale = 2)
    private BigDecimal area;  // жилая площадь в м²

    // Площадь участка (только для домов), хранится в м². 1 сотка = 100 м².
    @Column(name = "plot_area", precision = 10, scale = 2)
    private BigDecimal plotArea;

    @Column(name = "rooms")
    private Integer rooms;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @Column(name = "year_built")
    private Integer yearBuilt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<Listing> listings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Удобный метод для отображения полного адреса
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (district != null && district.getCity() != null) {
            sb.append(district.getCity().getName()).append(", ");
            sb.append(district.getName()).append(", ");
        }
        sb.append("ул. ").append(street).append(", д. ").append(buildingNumber);
        if (apartmentNumber != null && !apartmentNumber.isEmpty()) {
            sb.append(", кв. ").append(apartmentNumber);
        }
        return sb.toString();
    }
}
