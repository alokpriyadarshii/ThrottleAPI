package com.redstone.quotaguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class QuotaGuardApplication {
  public static void main(String[] args) {
    SpringApplication.run(QuotaGuardApplication.class, args);
  }
}
