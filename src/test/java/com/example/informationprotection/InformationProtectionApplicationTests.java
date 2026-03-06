package com.example.informationprotection;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Эта строка загрузит application-test.properties
class InformationProtectionApplicationTests {
    @Test
    void contextLoads() {
        // Тест проверяет, загружается ли контекст приложения
    }
}
