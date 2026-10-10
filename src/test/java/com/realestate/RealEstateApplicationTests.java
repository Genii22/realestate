package com.realestate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RealEstateApplicationTests {

    @Test
    void contextLoads() {
        // Проверяем что Spring-контекст поднимается без ошибок
    }
}
