package com.redstone.quotaguard.domain;

/**
 * Rate limit policy: capacity tokens with a steady refill rate.
 *
 * @param name policy name
 * @param capacity maximum tokens in the bucket
 * @param refillTokensPerSecond refill rate (tokens per second)
 */
public record RateLimitPolicy(String name, long capacity, double refillTokensPerSecond) {
  public RateLimitPolicy {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must be > 0");
    }
    if (refillTokensPerSecond < 0.0d || Double.isNaN(refillTokensPerSecond) || Double.isInfinite(refillTokensPerSecond)) {
      throw new IllegalArgumentException("refillTokensPerSecond must be a finite number >= 0");
    }
  }
}
