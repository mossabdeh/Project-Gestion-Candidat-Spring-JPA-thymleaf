@admin @ui @P1
Feature: Admin interface quality
  As an administrator
  I want a clear and accessible interface
  So that I can use account management on desktop and mobile

  Scenario: Administrators can navigate using the mobile menu
    Given I am signed in as an admin
    When I open the administration dashboard on a mobile viewport
    Then the mobile navigation menu should be available
    And the mobile Administrators link should open the directory

  Scenario: Administrator creation form has associated labels and a cancel action
    Given I am signed in as an admin
    When I open the new administrator form
    Then the profile inputs should have accessible labels
    And cancelling should return to the administrator directory

  Scenario: Admin workspace uses the original warm brand colors
    Given I am signed in as an admin
    When I open the administration dashboard
    Then the dashboard should use the original coral branding and light navigation

  Scenario: Admin interface loads built Tailwind utility classes
    Given I am signed in as an admin
    When I open the administration dashboard
    Then the Admin topbar should use the compiled sticky utility
