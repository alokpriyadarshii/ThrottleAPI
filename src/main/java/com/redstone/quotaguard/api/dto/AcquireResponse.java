package com.redstone.quotaguard.api.dto;

public class AcquireResponse {
  private boolean allowed;
  private double remainingTokens;
  private long retryAfterMs;

  public AcquireResponse(boolean allowed, double remainingTokens, long retryAfterMs) {
    this.allowed = allowed;
    this.remainingTokens = remainingTokens;
    this.retryAfterMs = retryAfterMs;
  }

  public boolean isAllowed() {
    return allowed;
  }

  public double getRemainingTokens() {
    return remainingTokens;
  }

  public long getRetryAfterMs() {
    return retryAfterMs;
  }
}
