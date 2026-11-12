@candidate @P1
Feature: Candidate portal acceptance
  As a registered Candidate
  I want to access my competition workspace securely
  So that I can read announcements and official results only when available

  @smoke @auth
  Scenario: Candidate signs in and sees their own dashboard
    Given a browser-test Candidate account is created by an Admin
    When the browser-test Candidate signs in
    Then the Candidate dashboard identifies the signed-in account
    And the Candidate navigation offers competition information and my results

  @navigation
  Scenario: Candidate navigates to competition information
    Given I am signed in as a browser-test Candidate
    When I open competition information from the Candidate navigation
    Then I see the Candidate competition information page

  @results
  Scenario: Candidate can view the results page without exam publication
    Given I am signed in as a browser-test Candidate
    When I open my Candidate results
    Then the results page does not reveal grades or an admission decision

  @security
  Scenario: Candidate cannot open Admin account management
    Given I am signed in as a browser-test Candidate
    When I request the Admin account directory
    Then access to the Admin account directory is denied

  @auth
  Scenario: Candidate logout protects the dashboard
    Given I am signed in as a browser-test Candidate
    When I sign out of the Candidate portal
    Then the Candidate session is closed
    And opening the Candidate dashboard redirects to login

  @mobile
  Scenario: Candidate portal remains usable on a narrow mobile viewport
    Given I am signed in as a browser-test Candidate
    When I set the Candidate browser to a narrow mobile viewport
    Then the Candidate navigation and results link remain visible
    And the Candidate workspace has no horizontal overflow
