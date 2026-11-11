package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.AdminRepo;
import ntic.tlsi.gestiondoctorat2.repo.UserAccountRepo;
import ntic.tlsi.gestiondoctorat2.sec.DevelopmentAdminInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DevelopmentAdminInitializerTests {

    private final AdminRepo adminRepo = mock(AdminRepo.class);
    private final UserAccountRepo userAccountRepo = mock(UserAccountRepo.class);

    @Test
    void runsOnlyInDevProfile() {
        Profile profile = DevelopmentAdminInitializer.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertArrayEquals(new String[]{"dev"}, profile.value());
    }

    @Test
    void missingCredentialsNeverCreateAdmin() {
        new DevelopmentAdminInitializer(adminRepo, userAccountRepo, "", "").run();
        new DevelopmentAdminInitializer(adminRepo, userAccountRepo, "developer", "").run();
        new DevelopmentAdminInitializer(adminRepo, userAccountRepo, "", "strong-password-123").run();

        verifyNoInteractions(adminRepo, userAccountRepo);
    }

    @Test
    void weakPasswordIsRejected() {
        assertThrows(IllegalStateException.class,
                () -> new DevelopmentAdminInitializer(adminRepo, userAccountRepo, "developer", "short").run());
        assertThrows(IllegalStateException.class,
                () -> new DevelopmentAdminInitializer(adminRepo, userAccountRepo, "same-password", "same-password").run());

        verifyNoInteractions(adminRepo);
    }

    @Test
    void existingAdminIsNotOverwritten() {
        when(userAccountRepo.existsByUsername("developer")).thenReturn(true);

        new DevelopmentAdminInitializer(adminRepo, userAccountRepo, "developer", "strong-password-123").run();

        verify(adminRepo, never()).save(any());
    }

    @Test
    void createsAdminWithProvidedCredentials() {
        when(userAccountRepo.existsByUsername("developer")).thenReturn(false);

        new DevelopmentAdminInitializer(adminRepo, userAccountRepo, "developer", "strong-password-123").run();

        org.mockito.ArgumentCaptor<Admin> captor = org.mockito.ArgumentCaptor.forClass(Admin.class);
        verify(adminRepo).save(captor.capture());
        Admin admin = captor.getValue();
        assertEquals("developer", admin.getUsername());
        assertEquals("strong-password-123", admin.getPassword());
        assertEquals(Role.ADMIN, admin.getTypeRole());
    }
}
