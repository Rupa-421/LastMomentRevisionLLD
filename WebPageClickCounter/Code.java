import java.util.concurrent.atomic.AtomicInteger;

public class VisitCounter{
    private final AtomicInteger[] counts;
    public VisitCounter(int n){
        counts = new AtomicInteger[n];
        for(int i=0;i<n;i++){
            counts[i] = new AtomicInteger(0);
        }
    }

    public void visit(int pageId){
        if(pageId<0 || pageId>=counts.length){
            throw new IllegalArgumentException("Invalid page");
        }
        counts[pageId].incrementAndGet();
    }

    public int getCount(int pageId){
        if(pageId<0 || pageId>=counts.length){
            throw new IllegalArgumentException("Invalid page");
        }
        return counts[pageId].get();
    }

    public int[] getAllCounts(){
        int[] result = new int[counts.length];
        for(int i=0;i<counts.length;i++){
            result[i]=counts[i].get();
        }
        return result;
    }
}