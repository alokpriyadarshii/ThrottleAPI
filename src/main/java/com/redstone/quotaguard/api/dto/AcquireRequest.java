package com.redstone.quotaguard.api.dto;

import jakarta.validation.constraints.Min;

public class AcquireRequest {
  @Min(1)
  private int permits = 1;

  public int getPermits() {
    return permits;
  }

  public void setPermits(int permits) {
    this.permits = permits;
  }
}
