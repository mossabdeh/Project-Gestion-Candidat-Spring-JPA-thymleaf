# Enrollment-scoped teacher grading (Phase 2c)

This workflow uses the enrollment-scoped paper and correction records introduced in Phase 2b. It is **separate from legacy Candidate-wide marking and results**.

## Manual test with dev accounts

1. As Vice Dean, create a competition. As Candidate, register; as Vice Dean, allocate the enrollment code.
2. As Admin, create at least **two teachers per subject**, and a **third** teacher for a subject if you plan to test the third-marker rule.
3. As CFD, open **Enrollment exam preparation**, initialize papers and allocate qualified teachers.
4. As a Teacher, open **My competition-scoped grading assignments** from the Teacher dashboard. Grade each assigned paper from 0 to 20 and save. Zero is a valid grade and is different from not graded.
5. Enter two grades differing by **at least 3.0** on one paper, then as CFD click **Assign third markers** on the enrollment exam preparation page. A third, distinct qualified teacher is assigned; that teacher can log in and enter the third mark.
6. Repeating third-marker assignment must not duplicate records. A gap below 3.0 must not generate a third assignment.

## Invariants

- Only the **assigned teacher** may edit their own enrollment correction; a posted teacher ID is never trusted.
- Grades must be finite numbers between 0 and 20 inclusive.
- Historical and published competitions cannot be graded or given new third-marker assignments.
- Third-marker allocation is protected by the CFD role, POST and CSRF validation.
- Correction rows are ordered by assignment ID to identify the first two markers consistently.
- All new data remains scoped to its competition enrollment.

## Remaining phases

The Candidate results page and CFD publication still read the **legacy** Candidate-wide marks. This phase does **not** publish enrollment-scoped grades or calculate final averages. Next: implement complete/third-mark validation, per-enrollment scoring, deterministic ranking, immutable publication snapshots and Candidate results reads. After that, migrate or retire legacy records explicitly.
