/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import java.io.ByteArrayInputStream;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class NewInputStream extends ServletInputStream {
  private ByteArrayInputStream buffer;

  public NewInputStream(ByteArrayInputStream buffer) {
    this.buffer = buffer;
  }

  //    InputBuffer ib = new InputBuffer();

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
}
