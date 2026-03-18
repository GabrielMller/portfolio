
package org.mule.extension.kafka.internal;


import org.mule.sdk.api.annotation.Configurations;
import org.mule.sdk.api.annotation.Extension;

import static org.mule.sdk.api.meta.JavaVersion.JAVA_17;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_21;

import org.mule.extension.kafka.internal.config.ConsumerConfig;
import org.mule.extension.kafka.internal.config.ProducerConfig;
import org.mule.sdk.api.annotation.dsl.xml.Xml;
import org.mule.sdk.api.annotation.JavaVersionSupport;

/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "kafka")
@Extension(name = "Kafka")
@JavaVersionSupport({JAVA_17, JAVA_21})
@Configurations({
  ProducerConfig.class,
  ConsumerConfig.class
})
public class KafkaExtension {

}
