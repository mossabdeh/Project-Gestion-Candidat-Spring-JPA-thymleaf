@admin @crud @P0
Feature: Manage Admin accounts
  As an authenticated administrator
  I want to manage Admin accounts through the browser
  So that account changes follow the expected business rules

  Scenario: Create an Admin with valid details
    Given I am signed in as an admin
    When I create a new Admin with valid details
    Then the new Admin should appear in the Admin list

  Scenario: Edit an Admin without changing their password
    Given I am signed in as an admin
    And I have created a separate Admin account
    When I edit that Admin's name and email
    Then the edited details should appear in the Admin list
    And the edited Admin should still be able to sign in

  Scenario: Delete another Admin
    Given I am signed in as an admin
    And I have created a separate Admin account
    When I confirm deletion of that Admin
    Then that Admin should no longer appear in the list
    And the deleted Admin should not be able to sign in

  Scenario: Reject a blank required name
    Given I am signed in as an admin
    When I submit an Admin creation form with a blank last name
    Then the last name validation message should be shown
    And the entered username should be preserved

  Scenario: Reject a duplicate username
    Given I am signed in as an admin
    And I have created a separate Admin account
    When I attempt to create another Admin with the same username
    Then the duplicate username validation message should be shown

  Scenario: Prevent deleting the currently signed-in Admin from the list
    Given I am signed in as an admin
    When I open my own Admin record in the list
    Then the Delete action should not be available for my account
