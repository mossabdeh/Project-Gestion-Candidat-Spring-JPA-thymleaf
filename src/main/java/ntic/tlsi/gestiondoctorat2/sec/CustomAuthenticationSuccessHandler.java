package ntic.tlsi.gestiondoctorat2.sec;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                response.sendRedirect("/adminPage");
                return;
            } else if (authority.getAuthority().equals("ROLE_CANDIDAT")) {
                response.sendRedirect("/candidatPage");
                return;
            } else if (authority.getAuthority().equals("ROLE_CFD")) {
                response.sendRedirect("/cfdPage");
                return;
            } else if (authority.getAuthority().equals("ROLE_VD")) {
                response.sendRedirect("/vdPage");
                return;
            }else if (authority.getAuthority().equals("ROLE_ENSEIGNANT")) {
                response.sendRedirect("/enseignantPage");
                return;
            }
        // If the user role doesn't match any specific redirect, fallback to a default page

    }
}}

