# Admin acceptance coverage roadmap

This is a **planned** inventory, not an assertion that tests have passed.

| ID | Area | Behavior | Status |
| --- | --- | --- | --- |
| ADM-01 | Auth | Valid login, invalid login, logout | Implemented in BDD |
| ADM-02 | Dashboard | Navigation to 5 management lists | Implemented in BDD |
| ADM-03 | Auth | Anonymous and non-admin access denied | Planned |
| ADM-04 | Admin CRUD | Create valid admin | Implemented in BDD |
| ADM-05 | Admin CRUD | Required last name and duplicate username | Implemented in BDD (other validation cases planned) |
| ADM-06 | Admin CRUD | Edit profile; preserve password | Implemented in BDD (role integrity handled by Java tests) |
| ADM-07 | Admin CRUD | Delete another admin; hide self-deletion | Implemented in BDD (direct POST/GET checks planned) |
| ADM-08 | Search | Matching, empty, and special-character searches | Planned (keyword filter used in CRUD tests) |
| ADM-09 | Pagination | First/last page, invalid indexes, preserved filters | Planned |
| ADM-10 | Candidat | Create, update and delete candidate | Planned |
| ADM-11 | Enseignant | Create, update and delete teacher | Planned |
| ADM-12 | Vice Doyen/CFD | List and edit role accounts | Planned |
| ADM-13 | Vice Doyen/CFD | Check UI Delete links against actual routes | Planned |
| ADM-14 | Data | Cross-role isolation and persisted CRUD changes | Planned |
| ADM-15 | UI | Feedback, keyboard accessibility, responsive layout | Planned |

Workflow: confirm expected business rule -> write Gherkin -> implement steps -> execute -> collect evidence -> open a bug for reproducible failures -> keep regression coverage.
