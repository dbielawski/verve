# Design choices
## High-throughput
- The system should be able to handle a large number of requests per second, hence the system should be designed to be highly scalable.
## Deduplication with Load Balancer Support (Extension 2)
- Use of Redis to count the number of unique requests and to store datetime as `yyyyMMddHHmm_<id>` as key and count as value.
- Entries don´t have expiration time, they are deleted when messages are sent to kafka.
## Use of Kafka to send unique requests to the backend
### Why Kafka and not message queue?
- Kafka allows message replay, which is useful for failure recovery and audits.
- Kafka’s partitioning enables easy horizontal scaling, making it suitable for high-throughput applications.

# Caveats
- Persistent Redis Entries: Redis entries persist beyond application restarts, so the service must handle deduplication correctly if the system is restarted.
- Scheduled Kafka Push: A cron job pushes unique request counts to Kafka every minute. This means there is a delay of up to one minute in reporting.
- Fault Tolerance: The system does not have built-in fault tolerance, so message data may be lost if the application fails before sending to Kafka.
- Duplicate Kafka Pushes: If the application is deployed in multiple instances (e.g., pods), each instance may try to push the same unique request counts to Kafka. A leader election or coordination mechanism is needed to ensure only one instance performs this task.

# Improvements
- Asynchronous Kafka Events: Each call could produce an individual Kafka event. A downstream service would then be responsible for counting unique requests and consolidating data before sending aggregate counts to Kafka.
- Fault Tolerance and Resilience: Consider adding mechanisms for Redis persistence and message delivery confirmation to prevent data loss in case of failure.

# Not production ready
- Testing: Unit and integration tests are required to validate system reliability and performance.
- Monitoring and Observability: Implement monitoring, logging, and alerting to detect and respond to issues in real-time.
- CI/CD: Add CI/CD pipelines to automate testing and deployment, ensuring reliable and fast delivery.
- .. and more
