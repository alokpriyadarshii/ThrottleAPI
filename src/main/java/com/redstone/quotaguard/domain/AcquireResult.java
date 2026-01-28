package com.redstone.quotaguard.domain;

/**
 * Result of a rate-limit acquisition.
 *
 * @param allowed whether the request is permitted
 * @param remainingTokens remaining tokens after acquisition attempt (if allowed), or current tokens (if not allowed)
 * @param retryAfterMs suggested wait time before retrying (0 if allowed)
 */
public record AcquireResult(boolean allowed, double remainingTokens, long retryAfterMs) {
  public static AcquireResult allowed(double remainingTokens) {
    return new AcquireResult(true, remainingTokens, 0);
  }

  public static AcquireResult rejected(double currentTokens, long retryAfterMs) {
    long safe = Math.max(0, retryAfterMs);
    return new AcquireResult(false, currentTokens, safe);
  }
}
