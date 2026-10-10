package com.realestate.service;

import com.realestate.entity.*;
import com.realestate.repository.ApplicationRepository;
import com.realestate.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ListingRepository listingRepository;

    /**
     * Анонимная заявка на просмотр — клиент оставляет имя и телефон,
     * учётной записи у него нет.
     */
    @Transactional
    public Application submitAnonymousApplication(Long listingId, String name,
                                                   String phone, String message) {
        if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Укажите имя и телефон для связи");
        }
        Listing listing = activeListing(listingId);

        Application application = new Application();
        application.setListing(listing);
        application.setApplicantName(name.trim());
        application.setApplicantPhone(phone.trim());
        application.setMessage(message);
        return applicationRepository.save(application);
    }

    private Listing activeListing(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Объявление не активно");
        }
        return listing;
    }

    @Transactional
    public Application updateStatus(Long applicationId, ApplicationStatus newStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        application.setStatus(newStatus);
        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<Application> getMyApplications(Long userId) {
        return applicationRepository.findByApplicantId(userId);
    }

    @Transactional(readOnly = true)
    public List<Application> getIncomingApplications(Long sellerId) {
        // Возвращаем ВСЕ заявки риелтора, чтобы он видел историю по объявлениям.
        return applicationRepository.findByListingSellerIdOrderByCreatedAtDesc(sellerId);
    }

    /**
     * Удалить заявку. Разрешено риелтору, чьему объявлению она адресована, или админу.
     */
    @Transactional
    public void deleteApplication(Long applicationId, Long currentUserId, boolean isAdmin) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        Long ownerId = application.getListing().getSeller().getId();
        if (!isAdmin && !ownerId.equals(currentUserId)) {
            throw new IllegalStateException("Можно удалять только заявки на свои объявления");
        }
        applicationRepository.delete(application);
    }
}
