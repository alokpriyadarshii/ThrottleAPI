package com.redstone.quotaguard.api;

import com.redstone.quotaguard.api.dto.AcquireRequest;
import com.redstone.quotaguard.api.dto.AcquireResponse;
import com.redstone.quotaguard.api.dto.StatusResponse;
import com.redstone.quotaguard.domain.AcquireResult;
import com.redstone.quotaguard.domain.BucketStatus;
import com.redstone.quotaguard.domain.RateLimitPolicy;
import com.redstone.quotaguard.service.AcquireService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LimitController {

  private final AcquireService service;

  public LimitController(AcquireService service) {
    this.service = service;
  }

  @PostMapping("/limits/{key}:acquire")
  public AcquireResponse acquire(@PathVariable String key, @Valid @RequestBody AcquireRequest req) {
    AcquireResult r = service.acquire(key, req.getPermits());
    return new AcquireResponse(r.allowed(), r.remainingTokens(), r.retryAfterMs());
  }

  @GetMapping("/limits/{key}")
  public StatusResponse status(@PathVariable String key) {
    BucketStatus s = service.status(key);
    return new StatusResponse(s.tokens(), s.capacity());
  }

  @GetMapping("/policy")
  public RateLimitPolicy policy() {
    return service.defaultPolicy();
  }
}
