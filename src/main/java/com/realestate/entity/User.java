package com.realestate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private Role role = Role.BUYER;

    // Специализация риелтора: RENT — аренда, SALE — продажа. Для админа — null.
    @Enumerated(EnumType.STRING)
    @Column(name = "specialization", length = 10)
    private DealType specialization;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ----------- Связи -----------
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Listing> listings = new ArrayList<>();

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL)
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    private List<Deal> deals = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Favorite> favorites = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
