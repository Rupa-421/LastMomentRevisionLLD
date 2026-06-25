Functional Requirements:
1.Allow X requests per second per user
2.Reject requests beyond limit
3.Support multiple algorithms in future
    Token Bucket
    Leaky Bucket
    Fixed Window

Non functional Requirements:
1.Thread-safe
2.Extensible
3.Low latency
4.Single machine for now
5.Memory Efficient for now
6.Easy to add new algorithms

Core components
Rate Limiter
TokeBucketRateLimiter
TokenBucket
RateLimiterFactory
RateLimitConfig

Design Patterns:
Strategy Pattern
Factory Design Pattern
Singleton Pattern

To check list of algorithms available check the hello interview rate Limiter

Multiple requests from same user?
ConcurrentHashMap
Why?
Thread safe
High throughput
computeIfAbsent
Atomic Bucket creation
synchronized bucket
Why?
Fine grained locking 
Only same user contends 
User A blocks only User A
User B blocks only User B

Extensibility
Add new algorithm:
class LeakyBucketLimiter implements RateLimiter 
No existing code changes because strategy design Pattern

Why Lru?
Inactive users automatically removal
Prevents memory leak 

Premium users should get more requests?

Bad Design:
class FreeTokenBucket{}
class PremiumTokenBucket{}
class VipTokenBucket{}

Better design:
public class RateLimitConfig{
    private int capacity;
    private int refillRate;
    public RateLimitConfig(int capacity,int refillRate){
        this.capacity = capacity;
        this.refillRate = refillRate;
    }
}
Now let's maintain Map<UserType,RateLimitConfig>
Map<UserType,RateLimitConfig> configs = new HashMap<>();
configs.put(UserType.FREE, new RateLimitConfig(10,10));
configs.put(UserType.PREMIUM, new RateLimitConfig(100,100));
public class User{
    private String id;
    private UserType userType;
}
Now bucket creation:
before:
 new TokenBucket(capacity,refillRate);
 Now:
 RateLimitConfig config =configs.get(user.getUserType());
 new TokenBucket(config.getCapacity,config.getRefillRate());
 