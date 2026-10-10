package com.realestate.service;

import com.realestate.entity.*;
import com.realestate.repository.DealRepository;
import com.realestate.repository.ListingRepository;
import com.realestate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    /**
     * Оформление сделки — транзакция обеспечивает атомарность:
     * либо и сделка создаётся, и объявление закрывается — либо ничего.
     */
    @Transactional
    public Deal completeDeal(Long listingId, Long buyerId, BigDecimal finalPrice) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Объявление уже закрыто или не активно");
        }
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Покупатель не найден"));

        // Создаём сделку
        Deal deal = new Deal();
        deal.setListing(listing);
        deal.setBuyer(buyer);
        deal.setFinalPrice(finalPrice);
        deal.setDealDate(LocalDate.now());
        deal.setStatus(DealStatus.COMPLETED);
        Deal savedDeal = dealRepository.save(deal);

        // Закрываем объявление (оба действия в одной транзакции)
        listing.setStatus(ListingStatus.CLOSED);
        listingRepository.save(listing);

        return savedDeal;
    }
}
