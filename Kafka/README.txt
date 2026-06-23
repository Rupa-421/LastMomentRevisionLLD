Requirements:

Functional Requirements:
Producers produce messages to a topic
Topics store messages in order (FIFO)
Consumers subscribe to Topics
Consumers read messages independently
Consumers resume from last read position (offset)
Support multiple consumers per topic

Non-functional requirements:
High concurrency
Thread safe message production and consumption
Ordered delivery per topic
Scalable consumer model

Core idea: Kafka is a log + offset system
We reduce the system to 3 concepts:

1.Topic = Append-only log
Stores messages in order
Immutable once written

2.Broker (KafkaController)
Manages topic
Routes publish/subscribe
Holds consumer offset state

3.Consumer = Pull based
Calls poll()
Reads from its offset
No dedicated thread per consumer

Strong hire should have:
1.Correct kafka abstraction
append-only log
offset based consumption

2.No thread explosion
No per-consumer thread

3.Clean seperation
Broker handles state
Consumer is dumb client
Producer only writes

4.No unnecessary controllers
No wait/notify complexity
Code still has long running task per consumer but that's part of the consumer application not the broker architecture
