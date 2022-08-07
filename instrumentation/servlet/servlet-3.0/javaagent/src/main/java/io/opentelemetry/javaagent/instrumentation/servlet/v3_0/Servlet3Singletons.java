/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.util.ClassAndMethod;
import io.opentelemetry.instrumentation.api.util.VirtualField;
import io.opentelemetry.javaagent.bootstrap.servlet.MappingResolver;
import io.opentelemetry.javaagent.instrumentation.servlet.ServletHelper;
import io.opentelemetry.javaagent.instrumentation.servlet.ServletInstrumenterBuilder;
import io.opentelemetry.javaagent.instrumentation.servlet.ServletRequestContext;
import io.opentelemetry.javaagent.instrumentation.servlet.ServletResponseContext;
import io.opentelemetry.javaagent.instrumentation.servlet.common.response.ResponseInstrumenterFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class Servlet3Singletons {
  private static final String INSTRUMENTATION_NAME = "io.opentelemetry.servlet-3.0";

  private static ByteArrayOutputStream cachedBytes;

  private static final Instrumenter<
          ServletRequestContext<HttpServletRequest>, ServletResponseContext<HttpServletResponse>>
      INSTRUMENTER =
          ServletInstrumenterBuilder.<HttpServletRequest, HttpServletResponse>create()
              .build(INSTRUMENTATION_NAME, Servlet3Accessor.INSTANCE);

  private static final ServletHelper<HttpServletRequest, HttpServletResponse> HELPER =
      new ServletHelper<>(INSTRUMENTER, Servlet3Accessor.INSTANCE);

  private static final VirtualField<Servlet, MappingResolver.Factory> SERVLET_MAPPING_RESOLVER =
      VirtualField.find(Servlet.class, MappingResolver.Factory.class);
  private static final VirtualField<Filter, MappingResolver.Factory> FILTER_MAPPING_RESOLVER =
      VirtualField.find(Filter.class, MappingResolver.Factory.class);

  private static final Instrumenter<ClassAndMethod, Void> RESPONSE_INSTRUMENTER =
      ResponseInstrumenterFactory.createInstrumenter(INSTRUMENTATION_NAME);

  public static ServletHelper<HttpServletRequest, HttpServletResponse> helper() {
    return HELPER;
  }

  public static Instrumenter<ClassAndMethod, Void> responseInstrumenter() {
    return RESPONSE_INSTRUMENTER;
  }

  public static MappingResolver getMappingResolver(Object servletOrFilter) {
    MappingResolver.Factory factory = getMappingResolverFactory(servletOrFilter);
    if (factory != null) {
      return factory.get();
    }
    return null;
  }

  public static String getRequestBody(HttpServletRequest request) {
    try {
      //      Class cls = request.getClass();
      //      Field requestClassField = cls.getDeclaredField("request");
      //
      //      if (cachedBytes == null) {
      //        cacheInputStream(request);
      //      }
      //
      //      ByteArrayInputStream buffer = new ByteArrayInputStream(cachedBytes.toByteArray());
      //      ServletInputStream newInputStream = new NewInputStream(buffer);
      //      requestClassField.setAccessible(true);
      //      Request internalRequest = (Request) requestClassField.get(request);
      //      Class requestClass = internalRequest.getClass();
      //      Field inputStream = requestClass.getDeclaredField("inputStream");
      //      inputStream.setAccessible(true);
      //
      //      inputStream.set(internalRequest, newInputStream);
      return CharStreams.toString(request.getReader());
    } catch (IOException e) {
      System.out.println("exception: " + e);
      return "";
    }
  }

  private static MappingResolver.Factory getMappingResolverFactory(Object servletOrFilter) {
    boolean servlet = servletOrFilter instanceof Servlet;
    if (servlet) {
      return SERVLET_MAPPING_RESOLVER.get((Servlet) servletOrFilter);
    } else {
      return FILTER_MAPPING_RESOLVER.get((Filter) servletOrFilter);
    }
  }

  private static void cacheInputStream(HttpServletRequest request) throws IOException {
    cachedBytes = new ByteArrayOutputStream();
    ByteStreams.copy(request.getInputStream(), cachedBytes);
  }

  private Servlet3Singletons() {}
}
