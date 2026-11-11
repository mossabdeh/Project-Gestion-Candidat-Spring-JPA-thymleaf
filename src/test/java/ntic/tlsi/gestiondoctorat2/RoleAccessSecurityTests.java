package ntic.tlsi.gestiondoctorat2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

@SpringBootTest
@AutoConfigureMockMvc
class RoleAccessSecurityTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void anonymousUserMustLogIn() throws Exception {
        mvc.perform(get("/adminPage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanOpenAdminPage() throws Exception {
        mvc.perform(get("/adminPage")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotOpenAdminPage() throws Exception {
        mvc.perform(get("/adminPage")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotCalculateResults() throws Exception {
        mvc.perform(post("/cfd/CalculResultat").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ENSEIGNANT")
    void teacherCannotGenerateCandidateCodes() throws Exception {
        mvc.perform(post("/vd/generateCodes").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotSubmitTeacherNotes() throws Exception {
        mvc.perform(post("/enseignant/saveNotes").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CFD")
    void cfdCannotManageAccountsThroughRestApi() throws Exception {
        mvc.perform(post("/Restcfd").with(csrf()).contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCannotUseCfdResultOperations() throws Exception {
        mvc.perform(post("/cfd/CalculResultat").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
