package com.realestate.service;

import com.realestate.entity.DealType;
import com.realestate.entity.Role;
import com.realestate.entity.User;
import com.realestate.repository.ListingRepository;
import com.realestate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final PasswordEncoder passwordEncoder;

    // Метод для Spring Security — ищем пользователя по email
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }

    @Transactional
    public User register(String firstName, String lastName, String email,
                         String rawPassword, String phone, Role role,
                         DealType specialization) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email уже занят: " + email);
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Пароль должен быть не короче 6 символов");
        }
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPhone(phone);
        user.setRole(role);
        // Специализация — только для риелторов (SELLER/AGENT).
        if (role == Role.SELLER || role == Role.AGENT) {
            user.setSpecialization(specialization);
        }
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));
    }

    /**
     * Удалить риелтора. Разрешено только админу.
     * Блокирует: удаление самого себя, удаление другого ADMIN,
     * удаление пользователя с активными объявлениями.
     */
    @Transactional
    public void deleteUser(Long userId, Long currentAdminId) {
        if (userId.equals(currentAdminId)) {
            throw new IllegalStateException("Нельзя удалить собственную учётную запись");
        }
        User target = findById(userId);
        if (target.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Нельзя удалить администратора");
        }
        long listingCount = listingRepository.countBySellerId(userId);
        if (listingCount > 0) {
            throw new IllegalStateException(
                    "У риелтора " + target.getFullName() + " ещё " + listingCount
                  + " объявлений. Удалите их сначала.");
        }
        userRepository.delete(target);
    }
}
