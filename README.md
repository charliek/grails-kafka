Grails Kafka
============

[![Circle CI](https://circleci.com/gh/charliek/grails-kafka/tree/master.svg?style=svg)](https://circleci.com/gh/charliek/grails-kafka/tree/master)

Disclaimer
----------
This is a very early plugin that should probably not be used by anybody. It is mostly
a way to setup my kafka [konsumer](https://github.com/charliek/konsumer) library, and
that library as well as this plugin should be considered very experimental. Along with
not working yet you will have to publish everything to maven local to get things running.

Usage
-----

Currently this plugin allows you to listen to a kafka topic by creating a service that
implements `com.charlieknudsen.konsumer.MessageProcessor` and uses the static `kafkaTopic`
name to declare the queue to listen to.  Then in configuration you need to wire the topic
up using something like:

```groovy
kafka {
	// The topic name should match the name in the service
	topic_name {
		// By default everything is disabled
		enabled = true
		// The zookeeper connection string
		zookeeper = '127.0.0.1:2181'
		// The number of partitions to listen to
		partitionThreads = 1
		// The number of worker threads to process incoming messages
		processingThreads = 11
		// The size of the in memory queue being processed by the processingThreads
		processingQueueSize = 30
		// The number of times to retry a message
		tryCount = 2
		// The consumer group name
		consumerGroup 'demo-consumer-name'
		props {
			// Add any raw properties that you want passed when connecting
			// See http://kafka.apache.org/08/configuration.html
			auto_offset_reset = 'smallest'
		}
	}
}
```

Then you need to create a service that looks something like:

```groovy
package example.myapp

import com.charlieknudsen.konsumer.MessageProcessor

class MessageHandlerService implements MessageProcessor {

	static kafkaTopic = 'my_topic'
	static transactional = false

	@Override
	void processMessage(byte[] bytes) throws Exception {
		log.warn("Got a message from kafka!! - ${new String(bytes)}")
	}
}
```

Remember that things might not be in a working state yet...
