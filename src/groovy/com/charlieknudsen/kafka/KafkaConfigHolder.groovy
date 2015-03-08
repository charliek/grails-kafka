package com.charlieknudsen.kafka

import com.charlieknudsen.konsumer.ListenerConfig
import groovy.util.logging.Log4j

@Log4j
class KafkaConfigHolder {

	ConfigObject kafkaConfig

	KafkaConfigHolder(ConfigObject kafkaConfig) {
		this.kafkaConfig = kafkaConfig
	}

	def getTopicConfig(String topicName) {
		return kafkaConfig."$topicName"
	}

	boolean isTopicEnabled(String topicName) {
		def topicConfig = getTopicConfig(topicName)
		// For now enabled must be explicitly set and we default to false
		boolean enabled =  topicConfig.enabled as boolean
		log.info("Kafka topic ${topicName} enabled: ${enabled}")
		return enabled
	}

	private void ifset(Closure c, def value) {
		value = value ?: null
		if ( value != null) {
			c(value)
		}
	}

	ListenerConfig getConfig(String topicName) {
		if (! isTopicEnabled(topicName)) {
			return null
		}
		ListenerConfig.Builder builder = new ListenerConfig.Builder()
				.topic(topicName)
		def topicConfig = getTopicConfig(topicName)
		ifset(builder.&partitionThreads, topicConfig.partitionThreads)
		ifset(builder.&processingThreads, topicConfig.processingThreads)
		ifset(builder.&processingQueueSize, topicConfig.processingQueueSize)
		ifset(builder.&tryCount, topicConfig.tryCount)
		ifset(builder.&consumerGroup, topicConfig.consumerGroup)
		ifset(builder.&zookeeper, topicConfig.zookeeper)
		ifset(builder.&consumerGroup, topicConfig.consumerGroup)
		topicConfig.props.each { key, value ->
			builder.setProperty(key.toString().replace('_', '.'), value.toString())
		}
		ListenerConfig config = builder.build()
		config.dumpConfig()
		return config
	}
}
