enum VehicleType{
    BIKE,
    CAR,
    TRUCK
}

enum SpotType{
    SMALL,
    MEDIUM,
    LARGE
}

enum TicketStatus{
    ACTIVE,
    PAID
}

abstract class Vehicle{
    private final String number;
    private final VehicleType type;
    protected Vehicle(String number,VehicleType type){
        this.number = number;
        this.type = type;
    }
}

class Bike extends Vehicle{
    public Bike(String number){
        super(number,VehicleType.BIKE);
    }
}

class ParkingSpot{
    private final int id;
    private final SpotType type;
    private Vehicle vehicle;
    public ParkingSpot(int id,SpotType type){
        this.id = id;
        this.type = type;
    }

    public boolean isAvailable(){
        return vehicle == null;
    }
    public void assignVehicle(Vehicle vehicle){
        this.vehicle = vehicle;
    }
    public void removeVehicle(){
        vehicle = null;
    }
}

class Ticket{
    private final String id;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;
    public Ticket(Vehicle vehicle,ParkingSpot spot){
        this.id = UUID.randomUUID().toString();
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = LocalDateTime.now();
    }
}

interface AllocationStrategy{
    ParkingSpot allocateSpot(Vehicle vehicle,Map<SpotType,Set<ParkingSpot>> availableSpots);
}

class FirstAvailableStrategy implements AllocationStrategy{
    @Override 
    public ParkingSpot allocateSpot(Vehicle vehicle,Map<SpotType,Set<ParkingSpot>> availableSpots){
        List<SpotType> preference = new ArrayList<>();
        switch(vehicle.getType()){
            case BIKE:
                preference = List.of(
                    SpotType.SMALL,
                    SpotType.MEDIUM,
                    SpotType.LARGE
                );
                break;
            case CAR:
                preference=List.of(
                    SpotType.MEDIUM,
                    SpotType.LARGE
                );
                break;
        }
        for(SpotType type:preference){
            Set<ParkingSpot> spots = availableSpots.get(type);
            if(spots!=null && !spots.isEmpty()){
                ParkingSpot spot = spots.iterator().next();
                spots.remove(spot);
                return spot;
            }
        }
        return null;
    }

}

interface FeeStrategy{
    double calculate(Ticket ticket);
}

class HourlyFeeStrategy implements FeeStrategy{
    private final double hourlyRate = 20;
    @Override 
    public double calculate(Ticket ticket){
        long hours = Duration.between(
            ticket.getEntryTime(),
            LocalDateTime.now()
        ).toHours();
        if(hours==0)
        hours=1;
        return hours*hourlyRate;
    }
}

class ParkingFloor{
    private final int floorNumber;
    private final Map<SpotType,Set<ParkingSpot>> availableSpots = new ConcurrentHashMap<>();
    public ParkingFloor(int floorNumber){
        this.floorNumber = floorNumber;
        for(SpotType type: SpotType.values()){
            availableSpots.put(type,ConcurrentHashMap.newKeySet());
        }
    }
    public void addSpot(ParkingSpot spot){
        availableSpots.get(spot.getType().add(spot));
    }
    public Map<SpotType,Set<ParkingSpot> getAvailableSpots(){
        return availableSpots;
    }
}

class ParkingLot{
    private final ParkingFloor floor;
    private final AllocationStrategy AllocationStrategy;
    private final FeeStrategy FeeStrategy;
    private final Map<String,Ticket> activeTickets = new ConcurrentHashMap<>();
    private final ReentrantLock lock  = new ReentrantLock();
    public ParkingLot(ParkingFloor floor,
    AllocationStrategy allocateSpotstrategy,FeeStrategy strategy){
        this.FeeStrategy = strategy;
        this.AllocationStrategy = allocateSpotstrategy;
        thils.floor = floor;
    }
    public Ticket parkVehicle(Vehicle vehicle){
        lock.lock();
        try{
            ParkingSpot spot = AllocationStrategy.allocateSpot(vehicle,floor.getAvailableSpots());
            if(spot==null)
            throw new RuntimeException("no spot available");
            spot.assignVehicle(vehicle);
            Ticket ticket = new Ticket(vehicle,spot);
            activeTickets.put(ticket.getId(),ticket);
            return ticket;
        }finally{
            lock.unlock();
        }
    }
    public double checkout(String ticketId){
        lock.lock();
        try{
            Ticket ticket = activeTickets.remove(ticketId);
            if(ticket == null){
                throw new RuntimeException("Invalid ticket");
            }
            double fee = FeeStrategy.calculate(ticket);
            ParkingSpot spot = ticket.getSpot();
            spot.removeVehicle();
            floor.getAvailableSpots().get(spot.SpotType()).add(spot);
            return fee;
        }
        finally{
            lock.unlock();
        }
    }
    public void showAvailableSpots(){
        for(SpotType type:SpotType.values()){
            System.out.println(type+" "+floor.getAvailableSpots().get(type).size());
        }
    }
}
