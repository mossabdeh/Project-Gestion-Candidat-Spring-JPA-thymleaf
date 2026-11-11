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

A lightweight baseline is committed as `src/main/resources/static/css/tailwind-admin.css`,
so `./mvnw spring-boot:run` works without Node. Run `npm run build` to generate
the fully optimized Tailwind CSS before deploying or after editing utilities.
Rebuild and commit the CSS after changing utility classes in Thymeleaf.
`npm run watch` enables continuous compilation.

GitHub Actions runs the build before Playwright BDD tests.
