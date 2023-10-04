package ntic.tlsi.gestiondoctorat2.sec;



import lombok.AllArgsConstructor;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.service.Iservice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecConfig  {
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
 SecurityFilterChain  securityFilterChain(HttpSecurity httpSecurity) throws Exception {
          httpSecurity.authorizeHttpRequests().
                  requestMatchers("/","/css/**","/js/**", "/img/**","/vendor/**","/h2-console/**").permitAll()
                  .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                  .and().headers(headers -> headers.frameOptions().disable()).csrf(csrf -> csrf
                          .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")))
          ;

          httpSecurity.formLogin().successHandler(customAuthenticationSuccessHandler());
          //httpSecurity.authorizeHttpRequests().requestMatchers("/**").hasRole(Role.CANDIDAT.name());
          httpSecurity.authorizeHttpRequests().anyRequest().authenticated();
          httpSecurity.exceptionHandling().accessDeniedPage("/notAuthorized");
          // authorize h2-console

          httpSecurity.userDetailsService(userDetailServiceImpl);




        return httpSecurity.build() ;
 }




    }

