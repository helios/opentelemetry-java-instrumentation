/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.tomcat.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.javaagent.bootstrap.servlet.AppServerBridge;
import io.opentelemetry.javaagent.instrumentation.servlet.ServletHelper;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.apache.coyote.Request;
import org.apache.coyote.Response;

public class TomcatHelper<REQUEST, RESPONSE> {
  protected final Instrumenter<Request, Response> instrumenter;
  protected final TomcatServletEntityProvider<REQUEST, RESPONSE> servletEntityProvider;
  private final ServletHelper<REQUEST, RESPONSE> servletHelper;

  public TomcatHelper(
      Instrumenter<Request, Response> instrumenter,
      TomcatServletEntityProvider<REQUEST, RESPONSE> servletEntityProvider,
      ServletHelper<REQUEST, RESPONSE> servletHelper) {
    this.instrumenter = instrumenter;
    this.servletEntityProvider = servletEntityProvider;
    this.servletHelper = servletHelper;
  }

  public boolean shouldStart(Context parentContext, Request request) {
    return instrumenter.shouldStart(parentContext, request);
  }

  public Context start(Context parentContext, Request request) {
    Context context = instrumenter.start(parentContext, request);
    request.setAttribute(ServletHelper.CONTEXT_ATTRIBUTE, context);
    return context;
  }

  public void end(
      Request request, Response response, Throwable throwable, Context context, Scope scope) {
    if (scope == null) {
      return;
    }
    scope.close();

    if (throwable == null) {
      throwable = AppServerBridge.getException(context);
    }

    if (throwable != null || mustEndOnHandlerMethodExit(request)) {
      instrumenter.end(context, request, response, throwable);
    }
  }

  private boolean mustEndOnHandlerMethodExit(Request request) {
    REQUEST servletRequest = servletEntityProvider.getServletRequest(request);
    return servletRequest != null && servletHelper.mustEndOnHandlerMethodExit(servletRequest);
  }

  public void attachResponseToRequest(Request request, Response response) {
    REQUEST servletRequest = servletEntityProvider.getServletRequest(request);
    RESPONSE servletResponse = servletEntityProvider.getServletResponse(response);

    if (servletRequest != null && servletResponse != null) {
      servletHelper.setAsyncListenerResponse(servletRequest, servletResponse);
    }
  }

  public void attachRequestHeadersToSpan(Request request, Span span) {
    Map<String, String> requestHeaders = this.extractRequestHeaders(request);
    span.setAttribute("http.request.headers", this.serializeToString(requestHeaders));
  }

  public void attachResponseHeadersToSpan(Response response, Span span) {
    Map<String, String> responseHeaders = this.extractResponseHeaders(response);
    span.setAttribute("http.response.headers", this.serializeToString(responseHeaders));
  }

  private String serializeToString(Map<String, String> headers) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(headers);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  private Map<String, String> extractRequestHeaders(Request request) {
    Enumeration<String> requestHeaderNames = request.getMimeHeaders().names();
    Map<String, String> requestHeaders = new HashMap<>();

    if (requestHeaderNames != null) {
      while (requestHeaderNames.hasMoreElements()) {
        String headerName = requestHeaderNames.nextElement();
        requestHeaders.put(headerName, request.getHeader(headerName));
      }
    }

    return requestHeaders;
  }

  private Map<String, String> extractResponseHeaders(Response response) {
    Map<String, String> responseHeaders = new HashMap<>();
    Enumeration<String> responseHeaderNames = response.getMimeHeaders().names();
    if (responseHeaderNames != null) {
      while (responseHeaderNames.hasMoreElements()) {
        String headerName = responseHeaderNames.nextElement();
        responseHeaders.put(headerName, response.getMimeHeaders().getHeader(headerName));
      }
    }

    return responseHeaders;
  }
}
