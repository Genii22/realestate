package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "property_types")
@Getter
@Setter
@NoArgsConstructor
public class PropertyType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;  // Квартира, Дом, Офис, Участок, Комната

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL)
    private List<Property> properties = new ArrayList<>();
}
