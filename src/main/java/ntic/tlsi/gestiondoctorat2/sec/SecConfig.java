package ntic.tlsi.gestiondoctorat2.sec;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

/*

public class SecConfig  {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

 SecurityFilterChain  securityFilterChain(HttpSecurity http) throws Exception {
     http.csrf().disable().exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
             .sessionManagement().sessionCreationPolicy.authorizeRequests()
             .antMatchers("/admin/**").hasRole(Role.ADMIN.name())
             .antMatchers("/candidat/**").hasRole(Role.CANDIDAT.name())
             .antMatchers("/cfd/**").hasRole(Role.CFD.name())
             .antMatchers("/vd/**").hasRole(Role.VD.name())
             .antMatchers("/enseignant/**").hasRole(Role.ENSEIGNANT.name())
             .anyRequest().authenticated()
             .and()
             .formLogin()
             .and()
             .logout()
             .logoutSuccessUrl("/")
             .invalidateHttpSession(true)
             .deleteCookies("JSESSIONID");

 }


    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}*/
