package com.realestate.service;

import com.realestate.entity.*;
import com.realestate.repository.*;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final PropertyTypeRepository propertyTypeRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public Page<Listing> getActiveListings(int page, int size) {
        return listingRepository.findByStatus(
                ListingStatus.ACTIVE,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
    }

    @Transactional(readOnly = true)
    public Page<Listing> searchListings(String keyword, DealType dealType,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // Нормализуем пустую строку к null, чтобы IS NULL в запросе сработал чисто.
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return listingRepository.filter(kw, dealType, minPrice, maxPrice, pageable);
    }

    @Transactional(readOnly = true)
    public Listing getById(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено: " + id));
    }

    @Transactional
    public Listing getByIdAndIncrementViews(Long id) {
        Listing listing = getById(id);
        listingRepository.incrementViewsCount(id);
        return listing;
    }

    /**
     * Полное создание объявления риелтором: создаёт Property + Listing + (опц.) фото.
     */
    @Transactional
    public Listing createFullListing(Long sellerId,
                                      Long propertyTypeId, Long districtId,
                                      String street, String buildingNumber, String apartmentNumber,
                                      BigDecimal area, BigDecimal plotArea, Integer rooms,
                                      Integer floor, Integer totalFloors, Integer yearBuilt,
                                      String propertyDescription,
                                      String title, DealType dealType, BigDecimal price,
                                      List<MultipartFile> photos) {

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Риелтор не найден"));
        PropertyType type = propertyTypeRepository.findById(propertyTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Тип недвижимости не найден"));
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("Район не найден"));

        Property property = new Property();
        property.setType(type);
        property.setDistrict(district);
        property.setStreet(street);
        property.setBuildingNumber(buildingNumber);
        property.setApartmentNumber(apartmentNumber);
        property.setArea(area);
        property.setPlotArea(plotArea);
        property.setRooms(rooms);
        property.setFloor(floor);
        property.setTotalFloors(totalFloors);
        property.setYearBuilt(yearBuilt);
        property.setDescription(propertyDescription);
        property = propertyRepository.save(property);

        Listing listing = new Listing();
        listing.setProperty(property);
        listing.setSeller(seller);
        listing.setTitle(title);
        listing.setDealType(dealType);
        listing.setPrice(price);
        listing.setStatus(ListingStatus.ACTIVE);

        // Сохраняем фото на диск (первое — главное).
        if (photos != null) {
            int order = 0;
            for (MultipartFile photo : photos) {
                String url = fileStorageService.store(photo);
                if (url == null) continue;
                ListingImage img = new ListingImage();
                img.setListing(listing);
                img.setImageUrl(url);
                img.setIsPrimary(order == 0);
                img.setSortOrder(order++);
                listing.getImages().add(img);
            }
        }

        return listingRepository.save(listing);
    }

    @Transactional
    public Listing createListing(Listing listing, Long propertyId, Long sellerId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Объект не найден"));
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        listing.setProperty(property);
        listing.setSeller(seller);
        listing.setStatus(ListingStatus.ACTIVE);
        return listingRepository.save(listing);
    }

    /**
     * Обновляет существующее объявление + связанный объект.
     * Новые фото добавляются к существующим (не заменяют).
     * Доступ — владельцу или администратору.
     */
    @Transactional
    public Listing updateFullListing(Long listingId, Long currentUserId, boolean isAdmin,
                                      Long propertyTypeId, Long districtId,
                                      String street, String buildingNumber, String apartmentNumber,
                                      java.math.BigDecimal area, java.math.BigDecimal plotArea,
                                      Integer rooms, Integer floor, Integer totalFloors, Integer yearBuilt,
                                      String propertyDescription,
                                      String title, DealType dealType, java.math.BigDecimal price,
                                      List<MultipartFile> newPhotos) {

        Listing listing = getById(listingId);
        if (!isAdmin && !listing.getSeller().getId().equals(currentUserId)) {
            throw new IllegalStateException("Можно редактировать только свои объявления");
        }
        PropertyType type = propertyTypeRepository.findById(propertyTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Тип недвижимости не найден"));
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("Район не найден"));

        Property property = listing.getProperty();
        property.setType(type);
        property.setDistrict(district);
        property.setStreet(street);
        property.setBuildingNumber(buildingNumber);
        property.setApartmentNumber(apartmentNumber);
        property.setArea(area);
        property.setPlotArea(plotArea);
        property.setRooms(rooms);
        property.setFloor(floor);
        property.setTotalFloors(totalFloors);
        property.setYearBuilt(yearBuilt);
        property.setDescription(propertyDescription);

        listing.setTitle(title);
        listing.setDealType(dealType);
        listing.setPrice(price);

        // Добавляем новые фото в конец. Главным остаётся первое существующее
        // (если фото не было — первое из новых).
        boolean noExisting = listing.getImages().isEmpty();
        if (newPhotos != null) {
            int order = listing.getImages().size();
            int newOrder = 0;
            for (MultipartFile photo : newPhotos) {
                String url = fileStorageService.store(photo);
                if (url == null) continue;
                ListingImage img = new ListingImage();
                img.setListing(listing);
                img.setImageUrl(url);
                img.setIsPrimary(noExisting && newOrder == 0);
                img.setSortOrder(order++);
                listing.getImages().add(img);
                newOrder++;
            }
        }

        return listingRepository.save(listing);
    }

    /**
     * Удалить одно фото из объявления. Сохраняет цельность «главного»:
     * если удалили primary, делает primary первое оставшееся.
     */
    @Transactional
    public void deletePhoto(Long listingId, Long imageId, Long currentUserId, boolean isAdmin) {
        Listing listing = getById(listingId);
        if (!isAdmin && !listing.getSeller().getId().equals(currentUserId)) {
            throw new IllegalStateException("Можно править фото только своих объявлений");
        }
        ListingImage target = listing.getImages().stream()
                .filter(i -> i.getId().equals(imageId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Фото не найдено в объявлении"));

        boolean wasPrimary = Boolean.TRUE.equals(target.getIsPrimary());
        fileStorageService.deleteByUrl(target.getImageUrl());
        listing.getImages().remove(target);   // orphanRemoval=true → удалится из БД

        if (wasPrimary && !listing.getImages().isEmpty()) {
            listing.getImages().get(0).setIsPrimary(true);
        }
        listingRepository.save(listing);
    }

    /**
     * Полное удаление объявления. Удаляет фото с диска, само объявление,
     * заявки/сделки/избранное удаляются каскадно (ON DELETE CASCADE).
     * Доступ — владельцу объявления или администратору.
     */
    @Transactional
    public void deleteListing(Long listingId, Long currentUserId, boolean isAdmin) {
        Listing listing = getById(listingId);
        if (!isAdmin && !listing.getSeller().getId().equals(currentUserId)) {
            throw new IllegalStateException("Можно удалять только свои объявления");
        }
        // 1. Сначала удаляем файлы фото с диска.
        listing.getImages().forEach(img -> fileStorageService.deleteByUrl(img.getImageUrl()));
        // 2. Объект (Property) удаляем только если у него больше нет объявлений,
        //    чтобы не сломать историю в редких сценариях. В нашей модели Property
        //    создаётся вместе с объявлением, так что обычно удалится.
        Property property = listing.getProperty();
        listingRepository.delete(listing);
        listingRepository.flush();
        if (property != null && propertyRepository.findById(property.getId()).isPresent()
                && property.getListings().size() <= 1) {
            try {
                propertyRepository.delete(property);
            } catch (Exception ignored) {
                // Если property используется ещё где-то — оставляем висеть, не критично.
            }
        }
    }

    @Transactional
    public void closeListing(Long listingId) {
        Listing listing = getById(listingId);
        listing.setStatus(ListingStatus.CLOSED);
        listingRepository.save(listing);
    }

    @Transactional(readOnly = true)
    public List<Listing> getTopListings(int count) {
        return listingRepository.findTopByViews(PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<Listing> getMyListings(Long sellerId) {
        return listingRepository.findBySellerId(sellerId);
    }
}
