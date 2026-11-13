# Enrollment-scoped anonymization (migration phase 2a)

The original workflow stored a single `Candidat.code`. That cannot correctly represent the same Candidate participating in multiple competitions.

This change adds an independent `CompetitionEnrollment.anonymousCode` with a **unique pair of competition and code** in the database. It is intentionally **not yet connected to legacy exam grading**.

## Manual workflow (Spring dev profile)

1. Sign in as `dev-vd` and create a competition.
2. Sign in as `dev-candidate` and register on Competition details.
3. Sign in as `dev-vd` and choose **Competition registration codes** from the home page.
4. Click **Assign missing registration codes**. Refresh or submit again: existing codes stay unchanged.
5. Create a second competition and re-register the Candidate. The second enrollment gets its **own code**.

Only the latest, unpublished competition can be assigned new codes. The registration page and anonymization interface are separate from the legacy Candidate-wide code generation.

## Constraints

- Unique enrollment per Candidate and competition (introduced in PR #42).
- Unique non-null anonymous code per competition; the same code can be reused in different competitions.
- Codes use seven decimal digits (leading zero is allowed) and remain stable once assigned.
- Only `ROLE_VD` may list or allocate codes.
- Code allocation is a CSRF-protected POST and locks the competition to prevent concurrent allocation races.

## Still to migrate

- Link exam papers and correction assignments directly to enrollments.
- Update CFD scoring and ranking to work from enrollment-specific papers.
- Persist finalized immutable results and make Candidate results read from the publication snapshot.
- After compatibility verification, remove the old `Candidat.code` path.

**Do not use enrollment-specific codes with the legacy CFD exam setup:** that workflow is still tied to `Candidat.code`.
