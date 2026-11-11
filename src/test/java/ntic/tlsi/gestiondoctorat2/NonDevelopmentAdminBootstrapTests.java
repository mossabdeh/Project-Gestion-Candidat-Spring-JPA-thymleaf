package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.repo.UserAccountRepo;
import ntic.tlsi.gestiondoctorat2.sec.DevelopmentAdminInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "ADMIN_USERNAME=should-not-exist",
        "ADMIN_PASSWORD=strong-test-password-123"
})
class NonDevelopmentAdminBootstrapTests {

    @Autowired private ApplicationContext context;
    @Autowired private UserAccountRepo userAccountRepo;

    @Test
    void bootstrapIsDisabledOutsideDevEvenWithCredentials() {
        assertTrue(context.getBeansOfType(DevelopmentAdminInitializer.class).isEmpty());
        assertTrue(userAccountRepo.findByUsername("should-not-exist").isEmpty());
    }
}
