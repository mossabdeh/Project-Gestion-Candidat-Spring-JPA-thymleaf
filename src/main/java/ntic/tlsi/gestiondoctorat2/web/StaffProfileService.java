package ntic.tlsi.gestiondoctorat2.web;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.UserAccountRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Edit only allowlisted fields on a managed, correctly typed account. */
@Service
public class StaffProfileService {
    private final UserAccountRepo accounts;

    public StaffProfileService(UserAccountRepo accounts) {
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public TeacherProfileForm teacherForm(Long id) {
        return TeacherProfileForm.from(requireRole(id, Enseignant.class));
    }

    @Transactional(readOnly = true)
    public StaffProfileForm cfdForm(Long id) {
        return StaffProfileForm.from(requireRole(id, CFD.class));
    }

    @Transactional(readOnly = true)
    public StaffProfileForm viceDeanForm(Long id) {
        return StaffProfileForm.from(requireRole(id, VD.class));
    }

    @Transactional
    public void updateTeacher(TeacherProfileForm profile) {
        Enseignant teacher = requireRole(profile.getId(), Enseignant.class);
        updateIdentity(teacher, profile);
        teacher.setGrade(profile.getGrade().trim());
        teacher.setSpecialite(profile.getSpecialite());
    }

    @Transactional
    public void updateCfd(StaffProfileForm profile) {
        updateIdentity(requireRole(profile.getId(), CFD.class), profile);
    }

    @Transactional
    public void updateViceDean(StaffProfileForm profile) {
        updateIdentity(requireRole(profile.getId(), VD.class), profile);
    }

    private void updateIdentity(User account, StaffProfileForm profile) {
        String username = profile.getUsername().trim();
        if (accounts.existsByUsernameAndIdNot(username, account.getId())) {
            throw new UsernameAlreadyUsedException();
        }
        account.setUsername(username);
        account.setNom(profile.getNom().trim());
        account.setPrenom(profile.getPrenom().trim());
        account.setEmail(profile.getEmail().trim());
        // Password, role, dates and JPA associations stay untouched.
    }

    private <T extends User> T requireRole(Long id, Class<T> role) {
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account ID required");
        User account = accounts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!role.isInstance(account)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found for this role");
        }
        return role.cast(account);
    }

    public static class UsernameAlreadyUsedException extends RuntimeException {}
}
