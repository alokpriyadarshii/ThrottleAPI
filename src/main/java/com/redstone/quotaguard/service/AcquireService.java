package com.redstone.quotaguard.service;

import com.redstone.quotaguard.config.QuotaGuardProperties;
import com.redstone.quotaguard.domain.AcquireResult;
import com.redstone.quotaguard.domain.BucketStatus;
import com.redstone.quotaguard.domain.RateLimitPolicy;
import com.redstone.quotaguard.domain.RateLimiter;
import org.springframework.stereotype.Service;

@Service
public class AcquireService {

  private final RateLimiter limiter;
  private final QuotaGuardProperties props;

  public AcquireService(RateLimiter limiter, QuotaGuardProperties props) {
    this.limiter = limiter;
    this.props = props;
  }

  public AcquireResult acquire(String key, int permits) {
    return limiter.acquire(key, defaultPolicy(), permits);
  }

  public BucketStatus status(String key) {
    return limiter.status(key, defaultPolicy());
  }

  public RateLimitPolicy defaultPolicy() {
    return new RateLimitPolicy(
        "default",
        props.getDefaultPolicy().getCapacity(),
        props.getDefaultPolicy().getRefillTokensPerSecond());
  }
}
