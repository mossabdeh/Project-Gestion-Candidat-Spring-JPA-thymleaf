package ntic.tlsi.gestiondoctorat2.sec;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
public class SecConfig {
    private UserDetailServiceImpl userDetailServiceImpl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, Environment environment) throws Exception {
        boolean localDevelopment = environment.acceptsProfiles(Profiles.of("dev"));
        httpSecurity.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/css/**", "/js/**", "/img/**",
                        "/vendor/**").permitAll()
                // Explicitly deny the database console outside the local dev profile.
                .requestMatchers("/h2-console/**").access((authentication, context) ->
                        new org.springframework.security.authorization.AuthorizationDecision(
                                localDevelopment && authentication.get().isAuthenticated()
                                && authentication.get().getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))))
                // Place admin account-management routes before role-specific routes.
                .requestMatchers(
                        "/adminPage", "/admin/**",
                        "/Restcfd", "/Restcfd/**",
                        "/Restenseignant", "/Restenseignant/**",
                        "/vdRest", "/vdRest/**",
                        "/candidat/getCandidats", "/candidat/CandidatAdd",
                        "/candidat/saveCandidat", "/candidat/delete",
                        "/candidat/editCandidat", "/candidat/saveEditedCandidat",
                        "/cfd/getCFDs", "/cfd/editCFD", "/cfd/saveEditedCFD",
                        "/vd/getVDs", "/vd/editVD", "/vd/saveEditedVD",
                        "/enseignant/getEnseignants", "/enseignant/EnseignantAdd",
                        "/enseignant/saveEnseignant", "/enseignant/delete",
                        "/enseignant/editEnseignant", "/enseignant/saveEditedEnseignant"
                ).hasRole("ADMIN")
                .requestMatchers("/candidatPage", "/candidat/**").hasRole("CANDIDAT")
                .requestMatchers("/cfdPage", "/cfd/**").hasRole("CFD")
                .requestMatchers("/vdPage", "/vd/**").hasRole("VD")
                .requestMatchers("/enseignantPage", "/enseignant/**").hasRole("ENSEIGNANT")
                .anyRequest().authenticated());

        // H2's frames and non-CSRF form submission are needed only in local dev.
        // The console is disabled outside dev and restricted to ADMIN within dev.
        if (localDevelopment) {
            httpSecurity.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
            httpSecurity.csrf(csrf -> csrf.ignoringRequestMatchers(
                    AntPathRequestMatcher.antMatcher("/h2-console/**")));
        }
        httpSecurity.formLogin(form -> form.successHandler(customAuthenticationSuccessHandler()));
        httpSecurity.exceptionHandling(exceptions -> exceptions.accessDeniedPage("/notAuthorized"));
        httpSecurity.userDetailsService(userDetailServiceImpl);

        return httpSecurity.build();
    }
}
