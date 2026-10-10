package com.realestate.controller;

import com.realestate.entity.DealType;
import com.realestate.entity.Listing;
import com.realestate.repository.DistrictRepository;
import com.realestate.repository.PropertyTypeRepository;
import com.realestate.service.ListingService;
import com.realestate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;
    private final UserService userService;
    private final DistrictRepository districtRepository;
    private final PropertyTypeRepository propertyTypeRepository;

    // Список объявлений с фильтрацией
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) DealType dealType,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Listing> listings = listingService.searchListings(
                keyword, dealType, minPrice, maxPrice, page, 9);
        model.addAttribute("listings", listings);
        model.addAttribute("keyword", keyword);
        model.addAttribute("dealType", dealType);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("dealTypes", DealType.values());
        return "listings/list";
    }

    // Карточка объявления — публичная, заявка анонимная.
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        Listing listing = listingService.getByIdAndIncrementViews(id);
        model.addAttribute("listing", listing);
        // Признак "можно удалять" — отдадим в шаблон, чтобы показать кнопку.
        boolean canDelete = false;
        if (userDetails != null) {
            var me = userService.findByEmail(userDetails.getUsername());
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            canDelete = isAdmin || listing.getSeller().getId().equals(me.getId());
        }
        model.addAttribute("canDelete", canDelete);
        return "listings/detail";
    }

    // Форма редактирования объявления.
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        Listing listing = listingService.getById(id);
        ensureCanEdit(listing, userDetails);
        model.addAttribute("listing", listing);
        model.addAttribute("propertyTypes", propertyTypeRepository.findAll());
        model.addAttribute("districts", districtRepository.findAll());
        return "listings/edit";
    }

    // Сохранение изменений.
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam Long propertyTypeId,
                         @RequestParam Long districtId,
                         @RequestParam String street,
                         @RequestParam(required = false) String buildingNumber,
                         @RequestParam(required = false) String apartmentNumber,
                         @RequestParam BigDecimal area,
                         @RequestParam(required = false) BigDecimal plotArea,
                         @RequestParam(required = false) Integer rooms,
                         @RequestParam(required = false) Integer floor,
                         @RequestParam(required = false) Integer totalFloors,
                         @RequestParam(required = false) Integer yearBuilt,
                         @RequestParam(required = false) String propertyDescription,
                         @RequestParam String title,
                         @RequestParam DealType dealType,
                         @RequestParam BigDecimal price,
                         @RequestParam(value = "photos", required = false) MultipartFile[] photos,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        try {
            var me = userService.findByEmail(userDetails.getUsername());
            boolean isAdmin = isAdmin(userDetails);
            List<MultipartFile> photoList = photos == null ? List.of() : Arrays.asList(photos);
            listingService.updateFullListing(id, me.getId(), isAdmin,
                    propertyTypeId, districtId,
                    street, buildingNumber, apartmentNumber,
                    area, plotArea, rooms, floor, totalFloors, yearBuilt,
                    propertyDescription, title, dealType, price, photoList);
            return "redirect:/listings/" + id + "?updated";
        } catch (IllegalStateException forbidden) {
            return "redirect:/listings/" + id + "?forbidden";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("listing", listingService.getById(id));
            model.addAttribute("propertyTypes", propertyTypeRepository.findAll());
            model.addAttribute("districts", districtRepository.findAll());
            return "listings/edit";
        }
    }

    // Удаление одной фотографии из объявления.
    @PostMapping("/{id}/photos/{imageId}/delete")
    public String deletePhoto(@PathVariable Long id,
                              @PathVariable Long imageId,
                              @AuthenticationPrincipal UserDetails userDetails) {
        var me = userService.findByEmail(userDetails.getUsername());
        boolean isAdmin = isAdmin(userDetails);
        try {
            listingService.deletePhoto(id, imageId, me.getId(), isAdmin);
        } catch (IllegalStateException forbidden) {
            return "redirect:/listings/" + id + "?forbidden";
        }
        return "redirect:/listings/" + id + "/edit?photoDeleted";
    }

    private void ensureCanEdit(Listing listing, UserDetails userDetails) {
        var me = userService.findByEmail(userDetails.getUsername());
        if (!isAdmin(userDetails) && !listing.getSeller().getId().equals(me.getId())) {
            throw new IllegalStateException("Нет прав на редактирование");
        }
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // Удаление объявления (POST /listings/{id}/delete).
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails) {
        var me = userService.findByEmail(userDetails.getUsername());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        try {
            listingService.deleteListing(id, me.getId(), isAdmin);
        } catch (IllegalStateException forbidden) {
            return "redirect:/listings/" + id + "?forbidden";
        }
        return "redirect:/profile?deleted";
    }

    // Форма размещения нового объявления (для риелторов).
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("propertyTypes", propertyTypeRepository.findAll());
        model.addAttribute("districts", districtRepository.findAll());
        return "listings/form";
    }

    // POST: создать объявление + объект + (опц.) фото.
    @PostMapping
    public String create(@RequestParam Long propertyTypeId,
                         @RequestParam Long districtId,
                         @RequestParam String street,
                         @RequestParam(required = false) String buildingNumber,
                         @RequestParam(required = false) String apartmentNumber,
                         @RequestParam BigDecimal area,
                         @RequestParam(required = false) BigDecimal plotArea,
                         @RequestParam(required = false) Integer rooms,
                         @RequestParam(required = false) Integer floor,
                         @RequestParam(required = false) Integer totalFloors,
                         @RequestParam(required = false) Integer yearBuilt,
                         @RequestParam(required = false) String propertyDescription,
                         @RequestParam String title,
                         @RequestParam DealType dealType,
                         @RequestParam BigDecimal price,
                         @RequestParam(value = "photos", required = false) MultipartFile[] photos,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        try {
            var seller = userService.findByEmail(userDetails.getUsername());
            List<MultipartFile> photoList = photos == null ? List.of() : Arrays.asList(photos);
            Listing saved = listingService.createFullListing(
                    seller.getId(),
                    propertyTypeId, districtId,
                    street, buildingNumber, apartmentNumber,
                    area, plotArea, rooms, floor, totalFloors, yearBuilt,
                    propertyDescription,
                    title, dealType, price,
                    photoList);
            return "redirect:/listings/" + saved.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("propertyTypes", propertyTypeRepository.findAll());
            model.addAttribute("districts", districtRepository.findAll());
            return "listings/form";
        }
    }
}
