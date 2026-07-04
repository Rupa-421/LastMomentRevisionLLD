Requirements:
There are n webpages in a website numbered 0 to n-1
Hundreds of users visit webpages of this website simultaneously
You have to record visit count for each page and return them when required

Questions:
Are visit counts only kept in memory? Yes
Can multiple threads call visit() simultaneously? Yes 
Can getCount() also be called simultaneously? Yes 
Do we only need total visit count? Yes 

Challenges:
Main challenge is thread safety

Class Design:
VisitCounter
counts[]
visit(pageId)
getCount(id)
getAllCounts()

Since pages are numbered 0 to n-1, an array is perfect.
Instead of int[] use AtomicInteger[]
Each increment becomes lock-free and thread-safe 

Interview Discussion
Why AtomicInteger instead of synchronized?
Using synchronized
public synchronized void visit(int pageId){
    counts[pageId]++;
}
This locks the entire object.
If 100 threads update different pages, they still wait for one another.
With AtomicInteger, every counter can be updated independently using atomic CPU operations(CAS), reducing contention
and improving scalability.

Followup 1:Million of Pages 
If n is extremely large(e.g., 10^9) but only a small subset of pages is visited,
allocating an array is wasteful.
Instead, use a concurrent map:
ConcurrentHashMap<Integer,AtomicInteger> counts = new ConcurrentHashMap<>();
public void visit(int pageId){
    counts.computeIfAbsent(pageId, k-> new AtomicInteger()).incrementAndGet();
}
This stores counters only for visited pages.

Followup 2: Even better under Heavy contention
If thousands of threads frequently update the same page, AtomicInteger can become a hotspot due to 
repeated CAS retries.
Java provides LongAdder, which spreads updates across multiple internal cells and combines them when reading.
ConcurrentHashMap<Integer,LongAdder> counts = new ConcurrentHashMap<>();
public void visit(int pageId){
    counts.computeIfAbsent(pageId,k-> new LongAdder()).increment();
}
public long getCount(int pageId){
    LongAdder adder = counts.get(pageId);
    return adder == null?0:adder.sum();
}

LongAdder usually offers higher throughput for write-heavy workloads, while
AtomicInteger is simpler and gives an immediately consistent value on every read.
