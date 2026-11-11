@admin @deletion @security @P0
Feature: Safe deletion of candidates and teachers
  As an administrator
  I want destructive actions to require confirmation and a protected POST
  So that accounts are not removed by opening a link

  Scenario Outline: GET requests never delete accounts
    Given I am signed in as an admin
    When I request "<path>" with GET
    Then the deletion endpoint should reject GET with status 405

    Examples:
      | path                |
      | /candidat/delete    |
      | /enseignant/delete  |

  Scenario Outline: Admin can confirm account deletion from the UI
    Given I am signed in as an admin
    And I created a "<role>" account for safe deletion testing
    When I confirm deleting that "<role>" account
    Then that "<role>" account should no longer appear in the filtered directory

    Examples:
      | role       |
      | candidate  |
      | teacher    |

  Scenario Outline: Cancelling confirmation leaves an account intact
    Given I am signed in as an admin
    And I created a "<role>" account for safe deletion testing
    When I cancel deleting that "<role>" account
    Then that "<role>" account should remain in the filtered directory

    Examples:
      | role       |
      | candidate  |
      | teacher    |
