package com.redstone.quotaguard.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

  public static final String HEADER_NAME = "X-API-Key";

  private final Set<String> allowedKeys;

  public ApiKeyAuthenticationFilter(Set<String> allowedKeys) {
    this.allowedKeys = allowedKeys;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String key = request.getHeader(HEADER_NAME);
    if (!StringUtils.hasText(key) || !allowedKeys.contains(key)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write("{\"error\":\"unauthorized\"}");
      return;
    }

    Authentication auth = new ApiKeyAuthToken(key);
    SecurityContextHolder.getContext().setAuthentication(auth);

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // allow liveness endpoints without a key
    return path.startsWith("/actuator/health") || path.startsWith("/actuator/info");
  }

  private static final class ApiKeyAuthToken extends AbstractAuthenticationToken {
    private final String apiKey;

    private ApiKeyAuthToken(String apiKey) {
      super(java.util.List.of(new SimpleGrantedAuthority("ROLE_API")));
      this.apiKey = apiKey;
      setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
      return apiKey;
    }

    @Override
    public Object getPrincipal() {
      return "apiKey";
    }
  }
}
