package com.realestate.config;

import com.realestate.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    // @Lazy breaks the circular dependency: SecurityConfig -> UserService -> PasswordEncoder -> SecurityConfig
    public SecurityConfig(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF отключён — формы из публичных страниц (анонимная заявка)
            // отправляются без токена. В рамках MVP это допустимо.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Публичные страницы — поиск, карточка, страница риелторов, вход
                .requestMatchers("/", "/listings", "/listings/{id}", "/realtors",
                                 "/auth/**", "/css/**", "/js/**", "/img/**",
                                 "/uploads/**").permitAll()
                // Анонимная отправка заявки на просмотр
                .requestMatchers(HttpMethod.POST, "/applications").permitAll()
                // Подтверждение / отклонение / удаление заявок — только риелторы и админ
                .requestMatchers("/applications/*/approve",
                                 "/applications/*/reject",
                                 "/applications/*/delete")
                    .hasAnyRole("SELLER", "AGENT", "ADMIN")
                // Кабинет риелтора
                .requestMatchers("/profile/**", "/favorites/**").authenticated()
                // Размещение / редактирование / удаление объявлений — риелторы и админ
                .requestMatchers("/listings/new", "/listings/*/edit",
                                 "/listings/*/update", "/listings/*/delete",
                                 "/listings/*/photos/*/delete")
                    .hasAnyRole("SELLER", "AGENT", "ADMIN")
                // Админка
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/profile", true)
                .failureUrl("/auth/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }
}
