package com.realestate.repository;

import com.realestate.entity.Role;
import com.realestate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    // Сложный запрос для защиты: количество объявлений у каждого продавца
    @Query("SELECT u, COUNT(l) FROM User u LEFT JOIN u.listings l " +
           "WHERE u.role = 'SELLER' GROUP BY u ORDER BY COUNT(l) DESC")
    List<Object[]> findSellersWithListingCount();
}
