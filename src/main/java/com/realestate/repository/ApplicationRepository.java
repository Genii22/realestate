package com.realestate.repository;

import com.realestate.entity.Application;
import com.realestate.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByApplicantId(Long applicantId);
    List<Application> findByListingId(Long listingId);
    List<Application> findByListingSellerIdAndStatus(Long sellerId, ApplicationStatus status);

    // Все заявки риелтора (любой статус), новые сверху.
    List<Application> findByListingSellerIdOrderByCreatedAtDesc(Long sellerId);

    boolean existsByListingIdAndApplicantId(Long listingId, Long applicantId);
}
