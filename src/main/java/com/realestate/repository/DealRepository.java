package com.realestate.repository;

import com.realestate.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    List<Deal> findByBuyerId(Long buyerId);
    List<Deal> findByListingSellerId(Long sellerId);

    // Сумма завершённых сделок по продавцу
    @Query("SELECT SUM(d.finalPrice) FROM Deal d WHERE d.listing.seller.id = :sellerId AND d.status = 'COMPLETED'")
    BigDecimal getTotalRevenueBySellerAndId(@Param("sellerId") Long sellerId);
}
