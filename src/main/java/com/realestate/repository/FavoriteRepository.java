package com.realestate.repository;

import com.realestate.entity.Favorite;
import com.realestate.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByUserId(Long userId);
    boolean existsByUserIdAndListingId(Long userId, Long listingId);
    void deleteByUserIdAndListingId(Long userId, Long listingId);
}
