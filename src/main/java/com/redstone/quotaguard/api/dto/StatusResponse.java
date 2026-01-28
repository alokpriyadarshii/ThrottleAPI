package com.redstone.quotaguard.api.dto;

public class StatusResponse {
  private double tokens;
  private long capacity;

  public StatusResponse(double tokens, long capacity) {
    this.tokens = tokens;
    this.capacity = capacity;
  }

  public double getTokens() {
    return tokens;
  }

  public long getCapacity() {
    return capacity;
  }
}
