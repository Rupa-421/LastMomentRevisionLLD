public interface RateLimiter{
    boolean allowRequest(String userId);
}

public class TokenBucket{
    private final int capacity;
    private final int refillRate;
    private double tokens;
    private long lastRefillRate;
    public TokenBucket{
        int capacity;
        int refilRate
    }{
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity;
        this.lastRefillRate = System.currentTimeMillis();
    }
    public synchronized boolean allowRequest(){
        refill();
        if(tokens>=1){
            tokens--;
            return true;
        }
        return false;
    }
    private void refill(){
        long now =System.currentTimeMillis();
        long elapsed = now - lastRefillRate;
        double tokensToAdd = (elapsed / 1000.0)* refillRate;
        tokens = Math.min(capacity,tokens+tokensToAdd);
        lastRefillRate = now;
    }
}

public class TokenBucketRateLimiter implements RateLimiter{
    private final int capacity;
    private final int refillRate;
    private final ConcurrentHashMap<String,TokenBucket> buckets;
    public TokenBucketRateLimiter(int capacity,int refillRate){
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.buckets = new ConcurrentHashMap<>();
    }
    @Override 
    public boolean allowRequest(String userId){
        TokenBucket bucket = buckets.computeIfAbsent(userId,id->new TokenBucket(capacity,refillRate));
        return bucket.allowRequest();
    }
}

Memory problem 
Map<userId,Bucket> keeps growing 
User LRU Cache

private final Map<String ,TokenBucket> buckets = Collections.synchronizedMap(new LinkedHashMap<>(1000,6.75f,true){
    @Override
    protected boolean remmoveEldestEntry(
        Map.Entry<String,TokenBucket> eldest){
            return size()>10000;
        }
});

public class RateLimiterFactory{
    public static RateLImiter create(Algorithm algorithm,int capacity,int refillRate){
        switch(algorithm){
            case TOKEN_BUCKET:
                return new TokenBucketRateLimiter(capacity,refillRate);
            case FIXED_WINDOW:
                return new FixedWindowRateLimiter(capacity,1000);
            default:
                throw new IllegalArgumentException("Unsupported");
        }
    }
}
public class FixedWindowRateLimiter implements RateLImiter{
    private final int limit;
    private final long windowSizeMillis;
    private final ConcurrentHashMap<String ,FixedWindow> userWindows;
    public FixedWindowRateLimiter(int limit,long windowSizeMillis){
        this.limit = limit;
        this.windowSizeMillis = windowSizeMillis;
        this.userWindows = new ConcurrentHashMap<>();
    }
    @Override
    public boolean allowRequest(String userId){
        FixedWindow window = userWindows.computeIfAbsent(userId,id->new FixedWindow(System.currentTimeMillis()));
        synchronized(window){
            long now = System.currentTimeMillis();
            if(now-window.windowStart>=windowSizeMillis){
                window.count =0;
                window.windowStart=now;
            }
            if(window.count>=limit){
                return false;
            }
            wndow.count++;
            return true;
        }
    }
}
public class FixedWindow{
    int count;
    long windowStart;
    public FixedWindow(int count,long windowStart){
        this.count = count;
        this.windowStart = windowStart;
    }
}