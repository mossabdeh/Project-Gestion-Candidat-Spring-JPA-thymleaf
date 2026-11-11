package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.User;
import ntic.tlsi.gestiondoctorat2.repo.UserAccountRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "ADMIN_USERNAME=development-admin",
        "ADMIN_PASSWORD=strong-development-password-123"
})
@AutoConfigureMockMvc
class DevelopmentAdminLoginTests {

    @Autowired private UserAccountRepo userAccountRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private MockMvc mvc;

    @Test
    void devAdminExistsWithHashedPassword() {
        User admin = userAccountRepo.findByUsername("development-admin").orElseThrow();

        assertNotEquals("strong-development-password-123", admin.getPassword());
        assertTrue(passwordEncoder.matches("strong-development-password-123", admin.getPassword()));
    }

    @Test
    void devAdminCanLogInWithConfiguredCredentials() throws Exception {
        mvc.perform(post("/login").with(csrf())
                        .param("username", "development-admin")
                        .param("password", "strong-development-password-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("development-admin"));
    }
}
