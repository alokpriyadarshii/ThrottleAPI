package com.redstone.quotaguard.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "quotaguard")
public class QuotaGuardProperties {

  private final DefaultPolicy defaultPolicy = new DefaultPolicy();
  private final Security security = new Security();
  private final Limiter limiter = new Limiter();

  public DefaultPolicy getDefaultPolicy() {
    return defaultPolicy;
  }

  public Security getSecurity() {
    return security;
  }

  public Limiter getLimiter() {
    return limiter;
  }

  public static class DefaultPolicy {
    private long capacity = 10;
    private double refillTokensPerSecond = 1.0;

    public long getCapacity() {
      return capacity;
    }

    public void setCapacity(long capacity) {
      this.capacity = capacity;
    }

    public double getRefillTokensPerSecond() {
      return refillTokensPerSecond;
    }

    public void setRefillTokensPerSecond(double refillTokensPerSecond) {
      this.refillTokensPerSecond = refillTokensPerSecond;
    }
  }

  public static class Security {
    private List<String> apiKeys = new ArrayList<>();

    public List<String> getApiKeys() {
      return apiKeys;
    }

    public void setApiKeys(List<String> apiKeys) {
      this.apiKeys = apiKeys;
    }
  }

  public static class Limiter {
    /** in-memory (default) or redis (future) */
    private String mode = "in-memory";

    public String getMode() {
      return mode;
    }

    public void setMode(String mode) {
      this.mode = mode;
    }
  }
}
