package com.redstone.quotaguard.api;

import com.redstone.quotaguard.api.dto.AcquireRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LimitControllerIT {

  @Autowired private TestRestTemplate rest;

  @Test
  void unauthorizedWhenMissingApiKey() {
    ResponseEntity<String> r = rest.getForEntity("/api/v1/policy", String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
  }

  @Test
  void acquireAndStatusWorkWithApiKey() {
    HttpHeaders h = new HttpHeaders();
    h.set("X-API-Key", "local-dev-key");
    h.setContentType(MediaType.APPLICATION_JSON);

    AcquireRequest req = new AcquireRequest();
    req.setPermits(1);

    HttpEntity<AcquireRequest> entity = new HttpEntity<>(req, h);
    ResponseEntity<String> acquire = rest.postForEntity("/api/v1/limits/test-user:acquire", entity, String.class);
    assertEquals(HttpStatus.OK, acquire.getStatusCode());
    assertNotNull(acquire.getBody());

    HttpEntity<Void> getEntity = new HttpEntity<>(h);
    ResponseEntity<String> status = rest.exchange("/api/v1/limits/test-user", HttpMethod.GET, getEntity, String.class);
    assertEquals(HttpStatus.OK, status.getStatusCode());
    assertNotNull(status.getBody());
  }
}
