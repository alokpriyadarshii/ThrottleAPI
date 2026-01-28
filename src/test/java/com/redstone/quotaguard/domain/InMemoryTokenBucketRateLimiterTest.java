package com.redstone.quotaguard.domain;

import com.redstone.quotaguard.infra.InMemoryTokenBucketRateLimiter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTokenBucketRateLimiterTest {

  @Test
  void allowsUpToCapacityThenRejectsWhenNoRefill() {
    RateLimiter limiter = new InMemoryTokenBucketRateLimiter();
    RateLimitPolicy policy = new RateLimitPolicy("t", 5, 0.0);

    assertTrue(limiter.acquire("k", policy, 5).allowed());
    AcquireResult r = limiter.acquire("k", policy, 1);
    assertFalse(r.allowed());
    assertEquals(0.0, r.remainingTokens(), 0.0001);
  }

  @Test
  void refillsOverTime() throws Exception {
    RateLimiter limiter = new InMemoryTokenBucketRateLimiter();
    RateLimitPolicy policy = new RateLimitPolicy("t", 2, 10.0); // 10 tokens/s

    assertTrue(limiter.acquire("k", policy, 2).allowed());
    assertFalse(limiter.acquire("k", policy, 1).allowed());

    Thread.sleep(150);

    AcquireResult r = limiter.acquire("k", policy, 1);
    assertTrue(r.allowed());

    BucketStatus s = limiter.status("k", policy);
    assertTrue(s.tokens() >= 0.0);
    assertEquals(2, s.capacity());
  }
}
