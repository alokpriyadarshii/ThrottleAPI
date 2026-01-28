package com.redstone.quotaguard.domain;

/**
 * Core domain port.
 */
public interface RateLimiter {
  AcquireResult acquire(String key, RateLimitPolicy policy, int permits);

  BucketStatus status(String key, RateLimitPolicy policy);
}
