# Manual test accounts (Spring `dev` profile)

The application uses Hibernate to generate its H2 tables. Do **not** use `schema.sql` to add users: `schema.sql` is for DDL, whereas this project uses a joined-inheritance `User` model and a `@PrePersist` password-hashing callback.

Instead, `DevelopmentRoleSeeder` creates five accounts using the normal repositories. The component only runs with the **dev profile** and **`DEV_SEED_ENABLED=true`**. It is disabled by default, including in regular application runs and CI.

## Start the development server

Choose your **own private password of at least 12 characters**. The examples use a placeholder; do not commit your real password.

**Git Bash / Linux / macOS**

```bash
export DEV_SEED_ENABLED=true
export DEV_SEED_PASSWORD='replace-with-your-private-password'
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Windows PowerShell**

```powershell
$env:DEV_SEED_ENABLED="true"
$env:DEV_SEED_PASSWORD="replace-with-your-private-password"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Visit `http://localhost:8082/login` and sign in with one of the usernames below. **All five use the password you set in `DEV_SEED_PASSWORD`.**

| Username | Role | Landing page |
| --- | --- | --- |
| `dev-admin` | Admin | `/adminPage` |
| `dev-candidate` | Candidate | `/candidatPage` |
| `dev-teacher` | Enseignant | `/enseignantPage` |
| `dev-cfd` | CFD | `/cfdPage` |
| `dev-vd` | Vice Dean | `/vdPage` |

The Candidate starts **without exam grades or an anonymization code**. The Teacher has the `ALGO` subject speciality. No mock official results are published. These are minimal accounts for manually exploring screens, not a complete competition lifecycle fixture.

## Behavior and safety

- The seeder inserts accounts **only when missing**, and it never resets existing passwords or overwrites profiles.
- If a seeded username already belongs to the wrong role, startup reports a conflict instead of modifying that user.
- Plain-text passwords are never stored by the seeder: the existing `User.@PrePersist` lifecycle callback hashes them when inserted.
- The repo's H2 URL is in-memory (`jdbc:h2:mem:doc`), so data resets after the server exits. Accounts will appear again on the next enabled startup.
- To disable this seeder, unset `DEV_SEED_ENABLED` or set it to `false`. The existing separate `ADMIN_USERNAME` / `ADMIN_PASSWORD` dev-admin mechanism remains supported.
- **Never enable these demonstration accounts on a shared/public server or in production**, and don't use real personal information as sample data.

## Tests

```bash
./mvnw test
```

`DevelopmentRoleSeederTests` checks opt-in restrictions, password validation, role types, idempotence, and username collision behavior.
