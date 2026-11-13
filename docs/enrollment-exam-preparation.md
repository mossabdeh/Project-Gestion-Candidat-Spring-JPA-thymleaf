# Enrollment-scoped exam preparation (Phase 2b)

## Why a separate paper model?

The legacy `Copie` entity is unique on **Candidate + subject**. It cannot hold the same Candidate's ALGO paper in two different competitions. Migrating that table in place would risk changing historical grades and breaking the old screens.

This phase adds:
- `EnrollmentExamPaper`: one paper per **enrollment + subject**, with a database uniqueness constraint.
- `EnrollmentExamCorrection`: one assigned teacher per **paper + teacher**, with nullable grade (not yet editable).
- `EnrollmentExamPreparationService`: transactionally creates papers and qualified marker assignments.
- A CFD page at `/cfd/enrollmentPapers` to initialize and inspect papers.

## Manual test using dev accounts

1. Sign in as `dev-vd` and create a competition.
2. Sign in as `dev-candidate` and register.
3. Sign in as `dev-vd`, open **Competition registration codes**, and generate the enrollment code.
4. As Admin, create **two ALGO teachers and two EDL teachers**. The default seeded Teacher alone isn't enough.
5. Sign in as `dev-cfd` and open **Enrollment exam preparation**.
6. Click **Initialize enrollment papers**. You should see two rows for the same code (ALGO and EDL).
7. Click **Assign qualified teachers**. Both rows should report two markers.
8. Repeat both actions; no new rows/assignments should appear.

The preparation service rejects missing codes, missing teacher coverage, old/published competitions and inconsistent existing assignments.

## Compatibility boundary

**This is an additive migration:** Legacy `Copie`, `Correction`, CFD grading, third-correction checks and Candidate result publication are unchanged. New `EnrollmentExamCorrection.grade` values are not edited through the legacy teacher screen. Do not treat new paper assignments as officially graded until the next migration.

## Follow-up

Move teacher grade entry, third-mark assignment and CFD aggregation to the enrollment-scoped entities. Then introduce final result snapshots by competition and update Candidate results reads. Only after that, retire the Candidate-wide exam tables and grade fields with an explicit data migration.
