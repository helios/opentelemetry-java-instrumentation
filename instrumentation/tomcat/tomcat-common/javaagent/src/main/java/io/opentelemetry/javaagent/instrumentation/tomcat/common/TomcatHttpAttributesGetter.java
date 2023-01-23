/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.tomcat.common;

import static io.opentelemetry.javaagent.instrumentation.tomcat.common.TomcatHelper.messageBytesToString;

import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerAttributesGetter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.json.JSONObject;

public class TomcatHttpAttributesGetter implements HttpServerAttributesGetter<Request, Response> {

  @Override
  public String getMethod(Request request) {
    return messageBytesToString(request.method());
  }

  @Override
  @Nullable
  public String getTarget(Request request) {
    String target = messageBytesToString(request.requestURI());
    String queryString = messageBytesToString(request.queryString());
    if (queryString != null) {
      target += "?" + queryString;
    }
    return target;
  }

  @Override
  @Nullable
  public String getScheme(Request request) {
    MessageBytes schemeMessageBytes = request.scheme();
    return schemeMessageBytes.isNull() ? "http" : messageBytesToString(schemeMessageBytes);
  }

  @Override
  public List<String> getRequestHeader(Request request, String name) {
    return Collections.list(request.getMimeHeaders().values(name));
  }

  @Override
  @Nullable
  public String getFlavor(Request request) {
    String flavor = messageBytesToString(request.protocol());
    if (flavor != null) {
      // remove HTTP/ prefix to comply with semantic conventions
      if (flavor.startsWith("HTTP/")) {
        flavor = flavor.substring("HTTP/".length());
      }
    }
    return flavor;
  }

  @Override
  @Nullable
  public Integer getStatusCode(Request request, Response response, @Nullable Throwable error) {
    return response.getStatus();
  }

  @Override
  public List<String> getResponseHeader(Request request, Response response, String name) {
    return Collections.list(response.getMimeHeaders().values(name));
  }

  @Override
  @Nullable
  public String getRoute(Request request) {
    return null;
  }

  @Nullable
  @Override
  public String requestHeaders(Request request, @Nullable Response response) {
    return toJsonString(mimeHeadersToMap(request.getMimeHeaders()));
  }

  @Nullable
  @Override
  public String responseHeaders(Request request, Response response) {
    return toJsonString(mimeHeadersToMap(response.getMimeHeaders()));
  }

  private static Map<String, String> mimeHeadersToMap(MimeHeaders headers) {
    return Collections.list(headers.names()).stream()
        .collect(Collectors.toMap(Function.identity(), headers::getHeader));
  }

  private static String toJsonString(Map<String, String> m) {
    return new JSONObject(m).toString();
  }
}
