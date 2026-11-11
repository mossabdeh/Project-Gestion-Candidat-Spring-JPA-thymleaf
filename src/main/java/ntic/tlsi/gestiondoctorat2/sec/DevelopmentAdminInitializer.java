package ntic.tlsi.gestiondoctorat2.sec;

import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.AdminRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Profile("dev")
public class DevelopmentAdminInitializer implements CommandLineRunner {

    private final AdminRepo adminRepo;
    private final String username;
    private final String password;

    public DevelopmentAdminInitializer(
            AdminRepo adminRepo,
            @Value("${ADMIN_USERNAME:}") String username,
            @Value("${ADMIN_PASSWORD:}") String password) {
        this.adminRepo = adminRepo;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        // No default credentials. Only the dev profile can create this account.
        if (username.isBlank() || password.isBlank()) {
            return;
        }
        if (password.length() < 12 || password.equalsIgnoreCase(username)) {
            throw new IllegalStateException(
                    "Development admin password must be at least 12 characters and differ from the username");
        }
        if (adminRepo.findByUsername(username).isPresent()) {
            return;
        }

        Admin admin = new Admin(username, password, "", "Development", "Admin", Role.ADMIN, new Date());
        // User.@PrePersist hashes the password once before saving.
        adminRepo.save(admin);
    }
}
