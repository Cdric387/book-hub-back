package com.bookhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BookHubApplicationTests {

    @Test
    void contextLoads() {
        // Vérifie que le contexte Spring Boot démarre sans erreur
    }
}
