/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import com.google.common.io.ByteStreams;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {
  private ByteArrayOutputStream cachedBytes;

  public MultiReadHttpServletRequest(HttpServletRequest request) {
    super(request);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (cachedBytes == null) {
      cacheInputStream();
    }

    ByteArrayInputStream buffer = new ByteArrayInputStream(cachedBytes.toByteArray());
    //    InputBuffer ib = new InputBuffer();
    return new ServletInputStream() {
      public int read() {

        return buffer.read();
      }

      public boolean isFinished() {
        return buffer.available() == 0;
      }

      public boolean isReady() {
        return true;
      }

      public void setReadListener(ReadListener readlistener) {}
    };
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(this.getInputStream()));
  }

  private void cacheInputStream() throws IOException {
    cachedBytes = new ByteArrayOutputStream();
    ByteStreams.copy(super.getInputStream(), cachedBytes);
  }
}
