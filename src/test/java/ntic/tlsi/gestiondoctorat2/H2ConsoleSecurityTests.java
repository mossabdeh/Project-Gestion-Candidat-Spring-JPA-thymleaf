package ntic.tlsi.gestiondoctorat2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class H2ConsoleSecurityTests {
    @Autowired MockMvc mvc;

    @Test
    void consoleIsNotPubliclyAccessible() throws Exception {
        mvc.perform(get("/h2-console/")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void consoleIsDeniedToAdminOutsideDev() throws Exception {
        mvc.perform(get("/h2-console/")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void consoleIsDeniedToOtherRoles() throws Exception {
        mvc.perform(get("/h2-console/")).andExpect(status().isForbidden());
    }
}
