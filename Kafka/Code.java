class Message{
    private final String value;
    public Message(String value){
        this.value = value;
    }
    public String getValue(){
        return this.value;
    }
}

class Topic{
    private final String id;
    private final String name;
    private final List<Message> messages = new ArrayList<>();
    private final Object lock;
    public Topic(String id,String name){
        this.id = id;
        this.name = name;
        this.lock = new Object();
    }
    public synchronized void append(Message message){
        messages.add(message);
    }
    public synchronized List<Message> readFrom(int offset){
        if(offset>= messages.size()){
            return Collections.emptyList();
        }
        return new ArrayList<>(messages.subList(offset,messages.size()));
    }
    public int size(){
        return messages.size();
    }
}

class KafkaController{
    private final Map<String ,Topic> topics = new ConcurrentHashMap<>();
    // topicId -> consumerId -> offset
    private final Map<String,Map<String,Integer>> offsets = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    public String createTopic(String name){
        String id= UUID.randomUUID().toString();
        topics.put(id,new Topic(id,name));
        offsets.put(id,new ConcurrentHashMap<>());
        return id;
    }
    public void publish(String topicId,Message message){
        Topic topic = topics.get(topicId);
        synchronized(topic.getLock()){
            topic.append(message);
            topic.getLock().notifyAll();
        }
    }
    public void subscribe(String topicId,String consumerId){
        offsets.get(topicId).putIfAbsent(consumerId,0);
    }
    public List<Message> poll(String topicId, String consumerId){
        Topic topic = topics.get(topicId);
        Map<String,Integer> topicOffsets = offsets.get(topicId);
        synchronized(topic.getLock()){
            int currentOffset = topicOffsets.get(consumerId);
            //We kept while loop because during spurious wakeups the condition may return false so we need to continously check
            while(currentOffset>=topic.size()){
                try{
                    topic.getLock().wait();
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                    return Collections.emptyList();
                }
            }
            List<Message> messages = topic.readFrom(currentOffset);
            topicOffsets.put(consumerId,currentOffset+messages.size());
            return messages;
        }
    }
    public void startConsumer(String topicId, String consumerId){
        executor.submit(()->{
            while(true){
                List<Message> messages = poll(topicId, consumerId);
                for(Message m:messages){
                    System.out.println("consumer "+ consumerId+" got: "+m.getValue());
                }
            }
        });
    }

    public void shutdown(){
        executor.shutdown();
    }
}
class Producer{
    private final KafkaController broker;
    public Producer(KafkaController broker){
        this.broker = broker;
    }
    public void send(String topicId , String value){
        broker.publish(topic,new Message(value));
    }
}
class Consumer{
    String id;
    public Consumer(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
}

