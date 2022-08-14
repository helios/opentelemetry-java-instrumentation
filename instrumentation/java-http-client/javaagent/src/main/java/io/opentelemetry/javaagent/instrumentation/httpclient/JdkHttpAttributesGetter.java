/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.httpclient;

import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientAttributesGetter;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.Nullable;

class JdkHttpAttributesGetter implements HttpClientAttributesGetter<HttpRequest, HttpResponse<?>> {

  @Override
  public String method(HttpRequest httpRequest) {
    return httpRequest.method();
  }

  @Override
  public String url(HttpRequest httpRequest) {
    return httpRequest.uri().toString();
  }

  @Override
  public List<String> requestHeader(HttpRequest httpRequest, String name) {
    return httpRequest.headers().allValues(name);
  }

  @Override
  @Nullable
  public Long requestContentLength(
      HttpRequest httpRequest, @Nullable HttpResponse<?> httpResponse) {
    return null;
  }

  @Override
  @Nullable
  public Long requestContentLengthUncompressed(
      HttpRequest httpRequest, @Nullable HttpResponse<?> httpResponse) {
    return null;
  }

  @Override
  public Integer statusCode(HttpRequest httpRequest, HttpResponse<?> httpResponse) {
    return httpResponse.statusCode();
  }

  @Override
  public String flavor(HttpRequest httpRequest, @Nullable HttpResponse<?> httpResponse) {
    if (httpResponse != null && httpResponse.version() == Version.HTTP_2) {
      return SemanticAttributes.HttpFlavorValues.HTTP_2_0;
    }
    return SemanticAttributes.HttpFlavorValues.HTTP_1_1;
  }

  @Override
  @Nullable
  public Long responseContentLength(HttpRequest httpRequest, HttpResponse<?> httpResponse) {
    return null;
  }

  @Override
  @Nullable
  public Long responseContentLengthUncompressed(
      HttpRequest httpRequest, HttpResponse<?> httpResponse) {
    return null;
  }

  @Override
  public List<String> responseHeader(
      HttpRequest httpRequest, HttpResponse<?> httpResponse, String name) {
    return httpResponse.headers().allValues(name);
  }

  @Nullable
  @Override
  public String requestHeaders(HttpRequest httpRequest, @Nullable HttpResponse<?> httpResponse) {
    return String.valueOf(httpRequest.headers().map());
  }

  @Nullable
  @Override
  public String responseHeaders(HttpRequest httpRequest, HttpResponse<?> httpResponse) {
    return String.valueOf(httpResponse.headers().map());
  }

  @Nullable
  @Override
  public String requestBody(HttpRequest httpRequest) {
    return null;
  }

  @Nullable
  @Override
  public String responseBody(HttpResponse<?> httpResponse) {
    if (httpResponse == null) {
      return null;
    }

    Object body = httpResponse.body();
    if (body == null) {
      return null;
    }
    if (body instanceof String) {
      return (String) body;
    } else if (body instanceof byte[]) {
      return new String((byte[]) body, StandardCharsets.UTF_8);
    }

    return String.valueOf(body);
  }
}
