package ntic.tlsi.gestiondoctorat2.sec;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Supplier;

/**
 * Optional accounts for manual testing. Never active outside the dev profile
 * and never active without the explicit dev.seed.enabled property.
 *
 * The User @PrePersist callback hashes passwords on first insertion.
 * Existing accounts are intentionally left unchanged on subsequent restarts.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "dev.seed.enabled", havingValue = "true")
public class DevelopmentRoleSeeder implements CommandLineRunner {
    private final UserAccountRepo users;
    private final AdminRepo admins;
    private final CandidatRepo candidates;
    private final EnseignantRepo teachers;
    private final CFDRepo cfds;
    private final VDRepo viceDeans;
    private final String password;

    public DevelopmentRoleSeeder(UserAccountRepo users,
                                 AdminRepo admins,
                                 CandidatRepo candidates,
                                 EnseignantRepo teachers,
                                 CFDRepo cfds,
                                 VDRepo viceDeans,
                                 @Value("${DEV_SEED_PASSWORD:}") String password) {
        this.users = users;
        this.admins = admins;
        this.candidates = candidates;
        this.teachers = teachers;
        this.cfds = cfds;
        this.viceDeans = viceDeans;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (password.isBlank() || password.length() < 12) {
            throw new IllegalStateException(
                    "DEV_SEED_PASSWORD must be set to at least 12 characters when dev.seed.enabled=true");
        }

        addIfMissing("dev-admin", Role.ADMIN, () -> admins.save(
                new Admin("dev-admin", password, "dev-admin@example.test",
                        "Demo", "Admin", Role.ADMIN, new Date())));
        addIfMissing("dev-candidate", Role.CANDIDAT, () -> candidates.save(
                new Candidat("dev-candidate", password, "dev-candidate@example.test",
                        "Demo", "Candidate", Role.CANDIDAT, new Date(), null, 0)));
        addIfMissing("dev-teacher", Role.ENSEIGNANT, () -> teachers.save(
                new Enseignant("dev-teacher", password, "dev-teacher@example.test",
                        "Demo", "Teacher", Role.ENSEIGNANT, "MCA", Matier.ALGO)));
        addIfMissing("dev-cfd", Role.CFD, () -> cfds.save(
                new CFD("dev-cfd", password, "dev-cfd@example.test",
                        "Demo", "CFD", Role.CFD, new Date())));
        addIfMissing("dev-vd", Role.VD, () -> viceDeans.save(
                new VD("dev-vd", password, "dev-vd@example.test",
                        "Demo", "Vice Dean", Role.VD, new Date())));
    }

    private void addIfMissing(String username, Role role, Supplier<? extends User> create) {
        users.findByUsername(username).ifPresentOrElse(existing -> {
            if (existing.getTypeRole() != role) {
                throw new IllegalStateException(
                        "Development account username conflict: " + username + " already has another role");
            }
        }, create::get);
    }
}
