/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.logback.mdc.v1_0;

import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.HELIOS_INSTRUMENTED_INDICATION;
import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.SPAN_ID;
import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.TRACE_FLAGS;
import static io.opentelemetry.instrumentation.api.log.LoggingContextConstants.TRACE_ID;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.util.VirtualField;
import io.opentelemetry.instrumentation.logback.v1_0.internal.UnionMap;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import java.util.HashMap;
import java.util.Map;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.matcher.ElementMatcher;

public class LoggingEventInstrumentation implements TypeInstrumentation {

  private static boolean heliosInstrumentedIndicator = false;
  private static void markInstrumentationIndicator() {
    Context parentContext = Context.current();
    Span span = Span.fromContext(parentContext);
    SpanContext parentSpanContext = span.getSpanContext();

    if (!span.isRecording() || !parentSpanContext.isValid() || heliosInstrumentedIndicator) {
      return;
    }

    span.setAttribute(HELIOS_INSTRUMENTED_INDICATION, "logback");
    heliosInstrumentedIndicator = true;
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed("ch.qos.logback.classic.spi.ILoggingEvent");
  }

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return implementsInterface(named("ch.qos.logback.classic.spi.ILoggingEvent"));
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod()
            .and(isPublic())
            .and(namedOneOf("getMDCPropertyMap", "getMdc"))
            .and(takesArguments(0)),
        LoggingEventInstrumentation.class.getName() + "$GetMdcAdvice");
  }

  @SuppressWarnings("unused")
  public static class GetMdcAdvice {

    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void onExit(
        @Advice.This ILoggingEvent event,
        @Advice.Return(typing = Typing.DYNAMIC, readOnly = false) Map<String, String> contextData) {
      if (contextData != null && contextData.containsKey(TRACE_ID)) {
        // Assume already instrumented event if traceId is present.
        return;
      }

      Context context = VirtualField.find(ILoggingEvent.class, Context.class).get(event);
      if (context == null) {
        return;
      }

      Span span = Java8BytecodeBridge.spanFromContext(context);
      SpanContext spanContext = span.getSpanContext();
      if (!spanContext.isValid()) {
        return;
      }

      markInstrumentationIndicator();

      Map<String, String> spanContextData = new HashMap<>();
      spanContextData.put(TRACE_ID, spanContext.getTraceId());
      spanContextData.put(SPAN_ID, spanContext.getSpanId());
      spanContextData.put(TRACE_FLAGS, spanContext.getTraceFlags().asHex());

      if (contextData == null) {
        contextData = spanContextData;
      } else {
        contextData = new UnionMap<>(contextData, spanContextData);
      }
    }
  }
}
