@admin @ui @P1
Feature: Vice Dean and CFD management pages
  As an administrator
  I want a consistent interface across account types
  So that I can manage accounts without switching design patterns

  Scenario Outline: Management directories use the new Admin layout
    Given I am signed in as an admin
    When I visit the "<path>" directory
    Then I should see the "<title>" directory in the Admin workspace
    And the directory should not show an unsupported Delete action

    Examples:
      | path         | title       |
      | /vd/getVDs   | Vice Deans  |
      | /cfd/getCFDs | CFD members |
