package com.redstone.quotaguard.infra;

import com.redstone.quotaguard.domain.AcquireResult;
import com.redstone.quotaguard.domain.BucketStatus;
import com.redstone.quotaguard.domain.RateLimiter;
import com.redstone.quotaguard.domain.RateLimitPolicy;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * In-memory token bucket implementation.
 *
 * Notes:
 * - Strong consistency per key within a single process.
 * - For horizontal scaling, swap with a distributed implementation (e.g., Redis/Lua) behind the same interface.
 */
@Component
public class InMemoryTokenBucketRateLimiter implements RateLimiter {

  private final ConcurrentMap<String, BucketState> buckets = new ConcurrentHashMap<>();

  @Override
  public AcquireResult acquire(String key, RateLimitPolicy policy, int permits) {
    validate(key, policy, permits);
    final long now = System.nanoTime();

    final ResultBox box = new ResultBox();

    buckets.compute(key, (k, existing) -> {
      BucketState state = existing == null
          ? BucketState.initial(policy.capacity(), now)
          : existing;

      BucketState refilled = refill(state, policy, now);

      if (refilled.tokens >= permits) {
        double remaining = refilled.tokens - permits;
        box.result = AcquireResult.allowed(remaining);
        return new BucketState(remaining, now);
      }

      long retryMs = computeRetryAfterMs(refilled.tokens, permits, policy.refillTokensPerSecond());
      box.result = AcquireResult.rejected(refilled.tokens, retryMs);
      return new BucketState(refilled.tokens, now);
    });

    return box.result;
  }

  @Override
  public BucketStatus status(String key, RateLimitPolicy policy) {
    Objects.requireNonNull(policy, "policy");
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("key must not be blank");
    }

    final long now = System.nanoTime();
    BucketState updated = buckets.compute(key, (k, existing) -> {
      BucketState state = existing == null
          ? BucketState.initial(policy.capacity(), now)
          : existing;
      BucketState refilled = refill(state, policy, now);
      return new BucketState(refilled.tokens, now);
    });

    return new BucketStatus(updated.tokens, policy.capacity());
  }

  private static void validate(String key, RateLimitPolicy policy, int permits) {
    Objects.requireNonNull(policy, "policy");
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("key must not be blank");
    }
    if (permits <= 0) {
      throw new IllegalArgumentException("permits must be > 0");
    }
  }

  private static BucketState refill(BucketState state, RateLimitPolicy policy, long nowNanos) {
    double tokens = state.tokens;
    double rate = policy.refillTokensPerSecond();

    if (rate <= 0.0d) {
      // no refill
      return new BucketState(Math.min(tokens, policy.capacity()), nowNanos);
    }

    long elapsedNanos = Math.max(0, nowNanos - state.lastRefillNanos);
    double elapsedSeconds = elapsedNanos / 1_000_000_000.0d;
    double added = elapsedSeconds * rate;
    double newTokens = Math.min(policy.capacity(), tokens + added);

    return new BucketState(newTokens, nowNanos);
  }

  private static long computeRetryAfterMs(double currentTokens, int permits, double ratePerSecond) {
    double deficit = permits - currentTokens;
    if (deficit <= 0) {
      return 0;
    }
    if (ratePerSecond <= 0.0d) {
      return Long.MAX_VALUE;
    }

    double seconds = deficit / ratePerSecond;
    double millis = seconds * 1000.0d;
    if (millis >= Long.MAX_VALUE) {
      return Long.MAX_VALUE;
    }
    return (long) Math.ceil(millis);
  }

  private record BucketState(double tokens, long lastRefillNanos) {
    static BucketState initial(long capacity, long now) {
      return new BucketState(capacity, now);
    }
  }

  private static final class ResultBox {
    private AcquireResult result;
  }
}
