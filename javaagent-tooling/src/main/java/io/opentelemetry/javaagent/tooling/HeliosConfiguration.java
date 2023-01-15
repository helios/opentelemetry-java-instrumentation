/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.tooling;

import static java.util.logging.Level.WARNING;

import java.util.Optional;
import java.util.logging.Logger;

public class HeliosConfiguration {

  private HeliosConfiguration() {}

  private static final Logger logger = Logger.getLogger(HeliosConfiguration.class.getName());
  public static final String HELIOS_TEST_TRIGGERED_TRACE = "hs-triggered-test";
  public static final String HELIOS_ENVIRONMENT_ENV_VAR = "HS_ENVIRONMENT";
  public static final String HELIOS_SERVICE_NAME_ENV_VAR = "HS_SERVICE_NAME";
  public static final String HELIOS_TOKEN_ENV_VAR = "HS_TOKEN";
  public static final String HELIOS_COLLECTOR_ENDPOINT_ENV_VAR = "HS_COLLECTOR_ENDPOINT";
  public static final String DEFAULT_COLLECTOR_ENDPOINT = "https://collector.heliosphere.io/traces";

  public static String getEnvironmentName() {
    return System.getenv(HELIOS_ENVIRONMENT_ENV_VAR);
  }

  public static String getServiceName() {
    String serviceName = System.getenv(HELIOS_SERVICE_NAME_ENV_VAR);
    if (serviceName == null) {
      logger.log(WARNING, "service name is mandatory and wasn't defined");
    }
    return serviceName;
  }

  public static String getHsToken() {
    return System.getenv(HELIOS_TOKEN_ENV_VAR);
  }

  public static String getCollectorEndpoint() {
    String result = System.getenv(HELIOS_COLLECTOR_ENDPOINT_ENV_VAR);
    return result == null ? DEFAULT_COLLECTOR_ENDPOINT : result;
  }

  public static Optional<Double> getHeliosSamplingRatioProperty() {
    try {
      String ratio = System.getenv(String.valueOf(RatioProperty.HS_SAMPLING_RATIO));
      if (ratio == null) {
        ratio = System.getProperty(RatioProperty.HS_SAMPLING_RATIO.propertyName());
      }
      if (ratio != null) {
        return Optional.of(Double.parseDouble(ratio));
      }
    } catch (RuntimeException e) {
      System.out.println("Exception while getting ratio property: " + e);
    }

    return Optional.empty();
  }

  private enum RatioProperty {
    HS_SAMPLING_RATIO("hs.sampling.ratio");

    private final String propertyName;

    RatioProperty(String propertyName) {
      this.propertyName = propertyName;
    }

    private String propertyName() {
      return propertyName;
    }
  }
}
