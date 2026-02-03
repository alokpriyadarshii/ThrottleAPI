package com.redstone.quotaguard.config;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, QuotaGuardProperties props) throws Exception {
    Set<String> keys = props.getSecurity().getApiKeys().stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(key -> !key.isBlank())
        .collect(java.util.stream.Collectors.toCollection(HashSet::new));

    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    if (keys.isEmpty()) {
      http.authorizeHttpRequests(auth -> auth
          .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
          .anyRequest().permitAll());
    } else {
      http.authorizeHttpRequests(auth -> auth
          .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
          .anyRequest().authenticated())
          .httpBasic(Customizer.withDefaults());
      http.addFilterBefore(new ApiKeyAuthenticationFilter(keys), UsernamePasswordAuthenticationFilter.class);
    }

    return http.build();
  }
}
