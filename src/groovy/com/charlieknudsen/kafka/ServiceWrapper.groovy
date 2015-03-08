package com.charlieknudsen.kafka

import com.charlieknudsen.konsumer.ListenerConfig
import com.charlieknudsen.konsumer.MessageProcessor
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.GrailsServiceClass

@Log4j
class ServiceWrapper {

	private static final String KAFKA_TOPIC = "kafkaTopic"

	private GrailsServiceClass service
	private KafkaConfigHolder configHolder
	String topic

	ServiceWrapper(def service, KafkaConfigHolder configHolder) {
		this.service = service
		this.configHolder = configHolder
	}

	boolean shouldProcess() {
		// For now assuming this will be a string or gstring
		log.info("looking for kafka topic on ${service}")
		topic = GrailsClassUtils.getStaticPropertyValue(service.clazz, KAFKA_TOPIC)?.toString()
		if (topic == null ) {
			return false
		}
		log.info("Topic ${topic} found on ${service}")
		if (! service instanceof MessageProcessor) {
			log.warn("Bean with topic named ${topic} does not extend MessageProcessor so skipping")
			return false
		}
		return configHolder.isTopicEnabled(topic)
	}

	ListenerConfig getConfig() {
		return configHolder.getConfig(topic)
	}

	static String underscoreToCamelCase(String underscore) {
		if(!underscore || underscore.isAllWhitespace()){
			return ''
		}
		return underscore.replaceAll(/_\w/){ it[1].toUpperCase() }
	}

	String getSpringBeanName() {
		String springTopicName = underscoreToCamelCase(topic.capitalize())
		String beanName = "kafkaListener${springTopicName}"
		log.info("Creating kafka listener bean for topic ${topic} named: kafkaListener${springTopicName}")
		return beanName
	}

}
