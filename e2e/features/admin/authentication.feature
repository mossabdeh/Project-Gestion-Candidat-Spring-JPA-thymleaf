@admin @auth @P0
Feature: Admin authentication
  As an administrator
  I want to sign in and out safely
  So that only authenticated staff access account management

  @smoke
  Scenario: Valid admin credentials open the dashboard
    Given I am on the login page
    When I sign in with valid admin credentials
    Then I should see the admin dashboard

  @smoke
  Scenario: Invalid admin credentials are rejected
    Given I am on the login page
    When I sign in with invalid credentials
    Then I should see the login error

  @smoke
  Scenario: Signing out protects the dashboard
    Given I am signed in as an admin
    When I sign out
    Then I should be signed out
    And I cannot reopen the admin dashboard without logging in
