@admin @ui @P1
Feature: Candidate and teacher account management UI
  As an administrator
  I want the account management interface to remain consistent
  So that I can navigate lists and forms across user types

  Scenario Outline: Account directories use the Admin layout
    Given I am signed in as an admin
    When I visit the "<path>" directory
    Then I should see the "<title>" directory in the Admin workspace

    Examples:
      | path                           | title      |
      | /candidat/getCandidats         | Candidates |
      | /enseignant/getEnseignants     | Teachers   |

  Scenario Outline: Account creation forms offer navigation and labeled fields
    Given I am signed in as an admin
    When I open the "<path>" account form
    Then I should see the "<title>" account form
    And the account form should have a Cancel action linking to "<returnPath>"

    Examples:
      | path                           | title         | returnPath                 |
      | /candidat/CandidatAdd          | Add Candidat  | /candidat/getCandidats     |
      | /enseignant/EnseignantAdd      | Add Enseignant| /enseignant/getEnseignants |

