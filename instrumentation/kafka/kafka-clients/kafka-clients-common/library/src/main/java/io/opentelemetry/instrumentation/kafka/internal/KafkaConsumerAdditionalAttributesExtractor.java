/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.kafka.internal;

import static io.opentelemetry.api.common.AttributeKey.longKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import javax.annotation.Nullable;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class KafkaConsumerAdditionalAttributesExtractor
    implements AttributesExtractor<ConsumerRecord<?, ?>, Void> {

  // TODO: remove this constant when this attribute appears in SemanticAttributes
  private static final AttributeKey<Long> MESSAGING_KAFKA_MESSAGE_OFFSET =
      longKey("messaging.kafka.message.offset");

  @Override
  public void onStart(
      AttributesBuilder attributes, Context parentContext, ConsumerRecord<?, ?> consumerRecord) {
    attributes.put(SemanticAttributes.MESSAGING_KAFKA_PARTITION, (long) consumerRecord.partition());
    attributes.put(MESSAGING_KAFKA_MESSAGE_OFFSET, consumerRecord.offset());
    if (consumerRecord.value() == null) {
      attributes.put(SemanticAttributes.MESSAGING_KAFKA_TOMBSTONE, true);
    }
  }

  @Override
  public void onEnd(
      AttributesBuilder attributes,
      Context context,
      ConsumerRecord<?, ?> consumerRecord,
      @Nullable Void unused,
      @Nullable Throwable error) {}
}
