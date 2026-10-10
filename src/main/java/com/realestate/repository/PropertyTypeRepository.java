package com.realestate.repository;

import com.realestate.entity.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyTypeRepository extends JpaRepository<PropertyType, Long> {
}
