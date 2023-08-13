/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.akkahttp.server;

import akka.http.scaladsl.model.HttpRequest;
import akka.http.scaladsl.model.HttpResponse;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerAttributesGetter;
import io.opentelemetry.javaagent.instrumentation.akkahttp.AkkaHttpUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.json.JSONObject;
import scala.Option;

class AkkaHttpServerAttributesGetter
    implements HttpServerAttributesGetter<HttpRequest, HttpResponse> {

  @Override
  public String getMethod(HttpRequest request) {
    return request.method().value();
  }

  @Override
  public List<String> getRequestHeader(HttpRequest request, String name) {
    return AkkaHttpUtil.requestHeader(request, name);
  }

  @Override
  @Nullable
  public String getRequestHeaders(HttpRequest request) {
    return toJsonString(
        StreamSupport.stream(request.getHeaders().spliterator(), false)
            .collect(
                Collectors.toMap(
                    akka.http.javadsl.model.HttpHeader::name,
                    akka.http.javadsl.model.HttpHeader::value)));
  }

  @Override
  public Integer getStatusCode(
      HttpRequest request, HttpResponse httpResponse, @Nullable Throwable error) {
    return httpResponse.status().intValue();
  }

  @Override
  public List<String> getResponseHeader(
      HttpRequest request, HttpResponse httpResponse, String name) {
    return AkkaHttpUtil.responseHeader(httpResponse, name);
  }

  @Override
  @Nullable
  public String getResponseHeaders(HttpRequest unused, HttpResponse httpResponse) {
    return toJsonString(
        StreamSupport.stream(httpResponse.getHeaders().spliterator(), false)
            .collect(
                Collectors.toMap(
                    akka.http.javadsl.model.HttpHeader::name,
                    akka.http.javadsl.model.HttpHeader::value)));
  }

  @Override
  public String getFlavor(HttpRequest request) {
    return AkkaHttpUtil.flavor(request);
  }

  @Override
  public String getTarget(HttpRequest request) {
    String target = request.uri().path().toString();
    Option<String> queryString = request.uri().rawQueryString();
    if (queryString.isDefined()) {
      target += "?" + queryString.get();
    }
    return target;
  }

  @Override
  @Nullable
  public String getRoute(HttpRequest request) {
    return null;
  }

  @Override
  public String getScheme(HttpRequest request) {
    return request.uri().scheme();
  }

  @Nullable
  private static String toJsonString(Map<String, String> m) {
    return new JSONObject(m).toString();
  }
}
