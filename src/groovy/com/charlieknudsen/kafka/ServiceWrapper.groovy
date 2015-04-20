package com.charlieknudsen.kafka

import com.charlieknudsen.konsumer.ListenerConfig
import com.charlieknudsen.konsumer.MessageProcessor
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.GrailsServiceClass

@Log4j
class ServiceWrapper {

	private static final String KAFKA_CONSUMER = "kafkaConsumer"

	private GrailsServiceClass service
	private KafkaConfigHolder configHolder
	String consumerName

	ServiceWrapper(def service, KafkaConfigHolder configHolder) {
		this.service = service
		this.configHolder = configHolder
	}

	boolean shouldProcess() {
		// For now assuming this will be a string or gstring
		log.debug("looking for kafka consumer on ${service}")
		consumerName = GrailsClassUtils.getStaticPropertyValue(service.clazz, KAFKA_CONSUMER)?.toString()
		if (consumerName == null ) {
			return false
		}
		log.info("Kafka consumer '${consumerName}' found on ${service}")
		if (! service instanceof MessageProcessor) {
			log.error("Bean with consumer named '${consumerName}' does not extend MessageProcessor so skipping")
			return false
		}
		return configHolder.isConsumerEnabled(consumerName)
	}

	ListenerConfig getConfig() {
		return configHolder.getConfig(consumerName)
	}

	String getTopicName() {
		return configHolder.getTopicName(consumerName)
	}

	static String underscoreToCamelCase(String underscore) {
		if(!underscore || underscore.isAllWhitespace()){
			return ''
		}
		return underscore.replaceAll(/_\w/){ it[1].toUpperCase() }
	}

	String getSpringBeanName() {
		String springTopicName = underscoreToCamelCase(consumerName.capitalize())
		String beanName = "kafkaListener${springTopicName}"
		log.info("Creating kafka listener bean for consumer ${consumerName} named: kafkaListener${springTopicName}")
		return beanName
	}

}
