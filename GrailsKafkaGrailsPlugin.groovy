import smartthings.kafka.KafkaConfigHolder
import smartthings.kafka.ServiceWrapper
import smartthings.kafka.WrappedKafkaListener
import smartthings.konsumer.MessageProcessor

class GrailsKafkaGrailsPlugin {
    // the plugin version
    def version = "0.0.4-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Grails Kafka Plugin"
    def author = "Charlie Knudsen"
    def authorEmail = "charlie.knudsen@smartthings.com"
    def description = '''\
Plugin for setting up kafka consumers.
'''

    def documentation = "https://github.com/charliek/grails-kafka"
    def license = "APACHE"
    def issueManagement = [ system: "github", url: "https://github.com/charliek/grails-kafka/issues" ]
    def scm = [ url: "https://github.com/charliek/grails-kafka" ]

    def doWithWebDescriptor = { xml ->
        // Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
		def kafkaConfig = application.config.kafka
		def configHolder = new KafkaConfigHolder(kafkaConfig)

		for(def service in application.serviceClasses) {
			ServiceWrapper wrappedService = new ServiceWrapper(service, configHolder)
			if (wrappedService.shouldProcess()) {
				log.info("Setting up ${service} as kafka consumer '${wrappedService.consumerName}' listening to topic '${wrappedService.topicName}'")
				"${wrappedService.springBeanName}"(WrappedKafkaListener, service.clazz, wrappedService.getConfig())
			}
		}
    }

    def doWithDynamicMethods = { ctx ->
        // Registering dynamic methods to classes
    }

    def doWithApplicationContext = { ctx ->
        // Post initialization spring config
		Map<String, WrappedKafkaListener> listenerBeans = ctx.getBeansOfType(WrappedKafkaListener)
		listenerBeans.each { k, v ->
			log.info("Starting kafka listener for bean ${k}")
			v.run((MessageProcessor) ctx.getBean(v.processorClass))
		}
    }

    def onChange = { event ->
        // Code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // Code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // Code that is executed when the application shuts down
		Map<String, WrappedKafkaListener> listenerBeans = event.ctx.getBeansOfType(WrappedKafkaListener)
		listenerBeans.each { k, v ->
			log.info("Shutting down listener for bean ${k}")
			v.shutdown()
		}
    }
}
