package com.charlieknudsen.kafka

import com.charlieknudsen.konsumer.KafkaListener
import com.charlieknudsen.konsumer.ListenerConfig
import com.charlieknudsen.konsumer.MessageProcessor

class WrappedKafkaListener {

	KafkaListener kafkaListener
	Class processorClass
	ListenerConfig config

	WrappedKafkaListener(Class processorClass, ListenerConfig config) {
		this.processorClass = processorClass
		this.config = config
		kafkaListener = new KafkaListener(config)
	}

	void run(MessageProcessor processor) {
		kafkaListener.run(processor)
	}

	void shutdown() {
		kafkaListener.shutdown()
	}
}
