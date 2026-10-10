package com.realestate.repository;

import com.realestate.entity.DealType;
import com.realestate.entity.Listing;
import com.realestate.entity.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    // Активные объявления с пагинацией
    Page<Listing> findByStatus(ListingStatus status, Pageable pageable);

    // Фильтрация по типу сделки
    Page<Listing> findByStatusAndDealType(ListingStatus status, DealType dealType, Pageable pageable);

    // Поиск по заголовку (LIKE)
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' AND " +
           "LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Listing> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Фильтрация по цене
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' " +
           "AND l.price BETWEEN :minPrice AND :maxPrice")
    Page<Listing> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice,
                                    Pageable pageable);

    // Комбинированный фильтр — все параметры опциональные (NULL = не применять).
    @Query("SELECT l FROM Listing l " +
           "WHERE l.status = 'ACTIVE' " +
           "AND (:keyword  IS NULL OR :keyword = '' " +
           "     OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:dealType IS NULL OR l.dealType = :dealType) " +
           "AND (:minPrice IS NULL OR l.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR l.price <= :maxPrice)")
    Page<Listing> filter(@Param("keyword")  String keyword,
                         @Param("dealType") DealType dealType,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         Pageable pageable);

    // Объявления конкретного продавца
    List<Listing> findBySellerId(Long sellerId);
    long countBySellerId(Long sellerId);

    // Увеличить счётчик просмотров
    @Modifying
    @Query("UPDATE Listing l SET l.viewsCount = l.viewsCount + 1 WHERE l.id = :id")
    void incrementViewsCount(@Param("id") Long id);

    // Топ объявлений по просмотрам
    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' ORDER BY l.viewsCount DESC")
    List<Listing> findTopByViews(Pageable pageable);

    // Статистика: средняя цена по типу сделки
    @Query("SELECT l.dealType, AVG(l.price), COUNT(l) FROM Listing l " +
           "WHERE l.status = 'ACTIVE' GROUP BY l.dealType")
    List<Object[]> getAveragePriceByDealType();
}
