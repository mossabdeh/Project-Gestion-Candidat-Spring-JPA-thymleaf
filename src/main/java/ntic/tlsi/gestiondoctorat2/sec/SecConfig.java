package ntic.tlsi.gestiondoctorat2.sec;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/css/**", "/js/**", "/img/**",
                        "/vendor/**", "/h2-console/**").permitAll()
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

        // Keep existing H2 console settings; hardening the console is a separate issue.
        httpSecurity.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        httpSecurity.csrf(csrf -> csrf.ignoringRequestMatchers(
                AntPathRequestMatcher.antMatcher("/h2-console/**")));
        httpSecurity.formLogin(form -> form.successHandler(customAuthenticationSuccessHandler()));
        httpSecurity.exceptionHandling(exceptions -> exceptions.accessDeniedPage("/notAuthorized"));
        httpSecurity.userDetailsService(userDetailServiceImpl);

        return httpSecurity.build();
    }
}
