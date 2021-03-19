package cz.muni.ics.perunproxyapi.presentation.rest.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.perunproxyapi.ga4gh.JWKSetPublishingEndpoint;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.WebConstants.NO_AUTH_PATH;

/**
 * Spring Security configuration.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${security.basicauth.path}")
    private String userFilesPath;

    private static final String ROLE_API_USER = "API_USER";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers('/' + JWKSetPublishingEndpoint.URL).permitAll()
                .antMatchers(NO_AUTH_PATH + "/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers( "/res/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        List<BasicAuthCredentials> credentials = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

            credentials = mapper.readValue(new File(userFilesPath), new TypeReference<>() {});
        } catch (IOException e) {
            log.warn("Reading user credentials from config was not successful", e);
        }

        for (BasicAuthCredentials user: credentials) {
            log.debug("Configuring credentials {} for Basic Auth with role {}.", user, ROLE_API_USER);
            auth.inMemoryAuthentication()
                    .withUser(user.getUsername())
                    .password(passwordEncoder().encode(user.getPassword()))
                    .roles(ROLE_API_USER);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Data
    @NoArgsConstructor
    private static class BasicAuthCredentials {
        @NonNull private String username;
        @NonNull private String password;

        @Override
        public String toString() {
            return "BasicAuthCredentials{" +
                    "username='" + username + '\'' +
                    ", password='*************'" +
                    '}';
        }
    }

}
