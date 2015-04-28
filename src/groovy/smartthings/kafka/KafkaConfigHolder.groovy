package smartthings.kafka

import groovy.util.logging.Log4j
import smartthings.konsumer.ListenerConfig

@Log4j
class KafkaConfigHolder {

	ConfigObject kafkaConfig

	KafkaConfigHolder(ConfigObject kafkaConfig) {
		this.kafkaConfig = kafkaConfig
	}

	def getConsumerConfig(String consumerName) {
		return kafkaConfig.consumers."$consumerName"
	}

	boolean isConsumerEnabled(String consumerName) {
		def consumerConfig = getConsumerConfig(consumerName)
		// For now enabled must be explicitly set and we default to false
		boolean enabled = consumerConfig.enabled as boolean
		log.info("Kafka consumer '${consumerName}' enabled: ${enabled}")
		return enabled
	}

	private void ifset(Closure c, def value) {
		value = value ?: null
		if (value != null) {
			c(value)
		}
	}

	String getTopicName(String consumerName) {
		def consumerConfig = getConsumerConfig(consumerName)
		return consumerConfig.topic ?: consumerName
	}

	ListenerConfig getConfig(String consumerName) {
		if (!isConsumerEnabled(consumerName)) {
			return null
		}
		def consumerConfig = getConsumerConfig(consumerName)
		def topicName = getTopicName(consumerName)
		log.info("Kafka consumer '${consumerName}' enabled and listening to topic '${topicName}'")
		ListenerConfig.Builder builder = new ListenerConfig.Builder()
				.topic(topicName)

		ifset(builder.&partitionThreads, consumerConfig.partitionThreads)
		ifset(builder.&processingThreads, consumerConfig.processingThreads)
		ifset(builder.&processingQueueSize, consumerConfig.processingQueueSize)
		ifset(builder.&tryCount, consumerConfig.tryCount)
		ifset(builder.&consumerGroup, consumerConfig.consumerGroup)
		ifset(builder.&zookeeper, consumerConfig.zookeeper)
		ifset(builder.&consumerGroup, consumerConfig.consumerGroup)

		consumerConfig.props.each { key, value ->
			builder.setProperty(key.toString().replace('_', '.'), value.toString())
		}
		ListenerConfig config = builder.build()
		config.dumpConfig()
		return config
	}
}
