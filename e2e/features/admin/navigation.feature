@admin @navigation @P1
Feature: Admin account-management navigation
  As an administrator
  I want to reach every available account-management section
  So that I can administer the application's users

  @smoke
  Scenario Outline: Admin dashboard opens the correct management list
    Given I am signed in as an admin
    When I open the "<section>" section
    Then I should be on the "<path>" management page

    Examples:
      | section     | path                       |
      | Admin       | /admin/getAdmins           |
      | Candidat    | /candidat/getCandidats     |
      | Vice Doyen  | /vd/getVDs                 |
      | CFD         | /cfd/getCFDs               |
      | Enseignant  | /enseignant/getEnseignants |
