# Tailwind for the Admin UI

Tailwind CSS v4 is compiled at build time; **no browser CDN**.
We intentionally omit Tailwind Preflight to preserve the existing Bootstrap
components. The original coral and charcoal colors remain unchanged.

From the repository root:

```bash
cd ui
npm install
npm run build
```

The generated `src/main/resources/static/css/tailwind-admin.css` is
committed, so running `./mvnw spring-boot:run` does not require Node.
Rebuild and commit the CSS after changing utility classes in Thymeleaf.
`npm run watch` enables continuous compilation.

GitHub Actions runs the build before Playwright BDD tests.
