/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.log4j.contextdata.v2_17;

import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.HELIOS_INSTRUMENTED_INDICATION;
import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.SPAN_ID;
import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.TRACE_FLAGS;
import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.TRACE_ID;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.util.ContextDataProvider;

/**
 * Implementation of Log4j 2's {@link ContextDataProvider} which is loaded via SPI. {@link
 * #supplyContextData()} is called when a log entry is created.
 */
public class OpenTelemetryContextDataProvider implements ContextDataProvider {

  private static boolean heliosInstrumentedIndicator = false;

  private static void markInstrumentationIndicator() {
    Context parentContext = Context.current();
    Span span = Span.fromContext(parentContext);
    SpanContext parentSpanContext = span.getSpanContext();

    if (!span.isRecording() || !parentSpanContext.isValid() || heliosInstrumentedIndicator) {
      return;
    }

    span.setAttribute(HELIOS_INSTRUMENTED_INDICATION, "log4j");
    heliosInstrumentedIndicator = true;
  }

  /**
   * Returns context from the current span when available.
   *
   * @return A map containing string versions of the traceId, spanId, and traceFlags, which can then
   *     be accessed from layout components
   */
  @Override
  public Map<String, String> supplyContextData() {
    Span currentSpan = Span.current();
    if (!currentSpan.getSpanContext().isValid()) {
      return Collections.emptyMap();
    }
    markInstrumentationIndicator();

    Map<String, String> contextData = new HashMap<>();
    SpanContext spanContext = currentSpan.getSpanContext();
    contextData.put(TRACE_ID, spanContext.getTraceId());
    contextData.put(SPAN_ID, spanContext.getSpanId());
    contextData.put(TRACE_FLAGS, spanContext.getTraceFlags().asHex());
    return contextData;
  }
}
