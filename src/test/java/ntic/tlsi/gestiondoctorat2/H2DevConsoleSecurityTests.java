package ntic.tlsi.gestiondoctorat2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class H2DevConsoleSecurityTests {
    @Autowired MockMvc mvc;
    @Autowired Environment environment;

    @Test
    void localDevEnablesConsoleWithoutRemoteConnections() {
        assertEquals("true", environment.getProperty("spring.h2.console.enabled"));
        assertEquals("false", environment.getProperty("spring.h2.console.settings.web-allow-others"));
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotAccessConsole() throws Exception {
        mvc.perform(get("/h2-console/")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminMayAccessConsoleInDev() throws Exception {
        // MockMvc does not dispatch to the H2 console servlet itself; check
        // that Spring Security allows the request past authorization.
        int statusCode = mvc.perform(get("/h2-console/")).andReturn().getResponse().getStatus();
        assertNotEquals(403, statusCode);
        assertNotEquals(302, statusCode);
    }
}
