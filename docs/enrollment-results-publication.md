# Enrollment-specific results publication (Phase 2d)

The Candidate profile and existing results page remain unchanged. This phase connects the enrollment-specific grading workflow to a durable publication snapshot.

## Flow

1. **Vice Dean:** create a competition, register candidates and allocate enrollment anonymization codes.
2. **CFD:** initialize enrollment papers and two qualified marker assignments.
3. **Teachers:** enter each assigned mark, including a third correction when needed.
4. **CFD:** go to **Enrollment exam preparation → Validate and publish competition results**.
5. Select **Validate and preview ranking**, review the subject averages and proposed admission decisions.
6. Select **Publish official results**. The service **revalidates and recalculates** under a competition lock, commits one immutable snapshot per enrollment, and sets the competition publication timestamp in the same transaction.
7. **Candidate:** open the existing **My results** page to see only the latest competition's published record for their account.

## Scoring policy

- Exactly two configured subjects, equally weighted.
- Exactly two marks per subject, unless their difference is **at least 3**, in which case exactly three complete marks are required.
- Each mark must be finite and between 0 and 20 inclusive; markers must be distinct and qualified for the subject.
- Subject and final averages are rounded to two decimals.
- Ranking: descending overall average, ties broken by ascending enrollment ID.
- Admission: first `NbrPostes` ranked enrollments.
- A missing mark, missing paper, unassigned third marker, or incomplete enrollment prevents **all** publication.

## Isolation and compatibility

The new snapshots are linked to a competition enrollment; they include the subjects, code, scores, rank, decision, and publication time captured at release. Candidate reads no longer fall back to Candidate-wide results when the latest competition has enrollments. Legacy Candidate-wide publication is blocked for an enrollment-based competition; older competitions with no enrollments retain their existing behavior.

**Limitations:** The Candidate results page shows the latest competition, not a history picker. Legacy grades already stored on `Candidat` are not migrated automatically. JPA's `updatable=false` and no public setters protect snapshots through this application's normal ORM path; database administrators still control raw database mutation. This is not a cryptographic audit log.

## Running tests

```bash
./mvnw test
```

`EnrollmentResultPublicationTests` exercises scoring, tie-breaks, isolation, rejection of invalid or incomplete grades, legacy-route protection, and access controls. The existing Candidate browser tests continue to use the same results page.
