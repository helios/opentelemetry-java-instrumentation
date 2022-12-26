/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.log4j.contextdata.v2_7;

import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.HELIOS_INSTRUMENTED_INDICATION;
import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.SPAN_ID;
import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.TRACE_FLAGS;
import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.TRACE_ID;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import java.util.List;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;

public final class SpanDecoratingContextDataInjector implements ContextDataInjector {

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
  private final ContextDataInjector delegate;

  public SpanDecoratingContextDataInjector(ContextDataInjector delegate) {
    this.delegate = delegate;
  }

  @Override
  public StringMap injectContextData(List<Property> list, StringMap stringMap) {
    StringMap contextData = delegate.injectContextData(list, stringMap);

    if (contextData.containsKey(TRACE_ID)) {
      // Assume already instrumented event if traceId is present.
      return contextData;
    }

    Span span = Java8BytecodeBridge.currentSpan();
    SpanContext currentContext = span.getSpanContext();
    if (!currentContext.isValid()) {
      return contextData;
    }

    markInstrumentationIndicator();

    StringMap newContextData = new SortedArrayStringMap(contextData);
    newContextData.putValue(TRACE_ID, currentContext.getTraceId());
    newContextData.putValue(SPAN_ID, currentContext.getSpanId());
    newContextData.putValue(TRACE_FLAGS, currentContext.getTraceFlags().asHex());
    return newContextData;
  }

  @Override
  public ReadOnlyStringMap rawContextData() {
    return delegate.rawContextData();
  }
}
