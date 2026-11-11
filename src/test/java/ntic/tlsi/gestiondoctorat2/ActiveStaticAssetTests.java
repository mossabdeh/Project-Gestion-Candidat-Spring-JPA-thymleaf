package ntic.tlsi.gestiondoctorat2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ActiveStaticAssetTests {
    private void requireAsset(String path) {
        assertNotNull(getClass().getClassLoader().getResource("static/" + path),
                "Active template asset must remain available: " + path);
    }

    @Test
    void originalRoleAndHomePageAssetsRemainAvailable() {
        for (String path : new String[] {
                "css/style.css", "js/main.js",
                "img/slide/slide-1.jpg", "img/slide/slide-2.jpg", "img/slide/slide-3.jpg",
                "img/team/profile.png", "img/about.jpg",
                "img/favicon.png", "img/apple-touch-icon.png",
                "vendor/animate.css/animate.min.css",
                "vendor/bootstrap/css/bootstrap.min.css",
                "vendor/bootstrap/js/bootstrap.bundle.min.js",
                "vendor/bootstrap-icons/bootstrap-icons.css",
                "vendor/bootstrap-icons/fonts/bootstrap-icons.woff2",
                "vendor/boxicons/css/boxicons.min.css",
                "vendor/boxicons/fonts/boxicons.woff2",
                "vendor/glightbox/css/glightbox.min.css",
                "vendor/glightbox/js/glightbox.min.js",
                "vendor/isotope-layout/isotope.pkgd.min.js",
                "vendor/swiper/swiper-bundle.min.css",
                "vendor/swiper/swiper-bundle.min.js",
                "vendor/purecounter/purecounter_vanilla.js",
                "vendor/waypoints/noframework.waypoints.js",
                "vendor/php-email-form/validate.js"
        }) requireAsset(path);
    }

    @Test
    void redesignedAdminAssetsRemainAvailable() {
        for (String path : new String[] {
                "css/admin.css", "css/admin-theme.css", "css/tailwind-admin.css",
                "vendor/bootstrap/css/bootstrap.min.css"
        }) requireAsset(path);
    }
}
