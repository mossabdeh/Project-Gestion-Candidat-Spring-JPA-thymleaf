# Cleanup audit — preserved dependencies

The cleanup removes unreferenced Eterna demo imagery and unused copies of vendor distributions, while preserving files linked by the current Thymeleaf pages. It does **not** redesign non-Admin pages.

- **Admin** depends on `css/admin.css`, `css/admin-theme.css`, compiled `css/tailwind-admin.css`, `img/favicon.png`, and Bootstrap.
- **Home and non-Admin role pages** still depend on `css/style.css`, `js/main.js`, Bootstrap, Animate.css, Bootstrap Icons, Boxicons, GLightbox, Isotope, Swiper, Purecounter, Waypoints, and the legacy form validation script.
- **Images retained**: hero slides, `img/about.jpg`, the team/profile placeholder, favicons. The removed blog/portfolio/client/testimonial stock images had no direct template or custom CSS/JS references.
- **Fonts** used by icon packages and minified vendor scripts/source maps are retained to avoid breaking dynamic resource loading and debugging.
- Removed a redundant `spring-boot-starter-thymeleaf` Maven dependency. No changes to H2, authorization, application routes, or Tailwind build inputs.

This first pass deliberately leaves all currently referenced libraries in place. Future migration of non-Admin pages should happen separately, with role-specific browser tests.
