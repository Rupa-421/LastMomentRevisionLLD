Functional Requirements:
1.Vehicle enters parking lot
2.Allocate parking spot
3.Generate  ticket
4.Checkout
5.Fee calculation
6.Release spot
7.Show available spots
8.Small vehicle can fit in big spot but not viceversa

Questions:
Is pricing hourly/base+hourly?
Do we need reservations
Do we need EV support
Is allocation nearest/random/first available
Single floor or multiple floors?
Multiple entry/exit gates?

Non functional Requirements:
Fast allocation -O(1)
Avoid scanning every spot Instead MAP<SpotType,Set<ParkingSpot>> instead of List<ParkingSpot>
Checkout should immediately return the spot to the available pool
Tomorrow we may support multiple floors,EV charging, Reservations, VIP Parking, Dynamic pricing - 
Design shouldn't change much
Pluggable allocation strategy 
Multiple vehicles may enter simultaneously

Datastructure
This deserves its own heading
Instead of Floor-List<Spot>
Prefer Floor Map<ParkingSpotType,Set<ParkingSpot>> why?
because faster lookup, faster remove, extensible

Why set? O(1) removal Instead of list.remove()
Why map? no scanning of all spots

Concurrency:
I would dedicate one heading
Critical section
Select spot
Remove spot
Generate ticket 
Syncrhonization can be ParkingFloor, Spot bucket(Best), ReentrantLock,ConcurrentHashMap
I used enums for VehicleType, SpotType, and TicketStatus because they represent a fixed, finite set of constants with no independent state or behavior. Enums provide compile-time type safety, efficient use as HashMap keys, clean switch statements, and avoid unnecessary object creation. If these concepts later acquire attributes or behaviors—for example, EV spot capabilities or vehicle dimensions—I would refactor them into classes without affecting the rest of the design. This follows the principle of using enums for constants and classes for entities with state and behavior.


1. EV Charging Support
Interview Answer

I would avoid changing the existing ParkingSpot class. Instead, I would introduce an EVParkingSpot that extends ParkingSpot and adds EV-specific properties like charger type, charging power, and charging status. I would also introduce an EVVehicle if needed. Since spot allocation already uses the Strategy pattern, I can simply add an EVAllocationStrategy that allocates only EV-enabled spots. The existing parking flow remains unchanged.

Classes Added
ParkingSpot
    ↑
EVParkingSpot

Vehicle
    ↑
EVVehicle

AllocationStrategy
      ↑
EVAllocationStrategy

Existing classes modified?

No.

2. Reservations
Interview Answer

I would introduce a Reservation entity and a ReservationService. Reserved spots would not remain in the available spot pool. During allocation, the strategy first checks whether the arriving vehicle has a valid reservation. If yes, it assigns the reserved spot; otherwise, it follows the normal allocation flow.

Classes Added
Reservation
ReservationService
ReservationStatus

Existing classes modified?

Only the allocation strategy is extended; the core parking flow remains unchanged.

3. VIP Parking
Interview Answer

I would add a new attribute such as isVIP to ParkingSpot or create a VIPParkingSpot subclass. Then I'd implement a VIPAllocationStrategy that prioritizes VIP spots for VIP customers while allowing normal allocation when appropriate. This avoids changing the existing parking logic.

Classes Added
VIPParkingSpot

or

boolean isVIP
4. Dynamic Pricing
Interview Answer

Since fee calculation already uses the Strategy pattern, I would simply implement another FeeStrategy. For example, WeekendPricingStrategy, PeakHourPricingStrategy, or EventPricingStrategy. At runtime, the appropriate strategy can be injected without changing the checkout logic.

Classes Added
FeeStrategy
      ↑
HourlyFeeStrategy

      ↑
WeekendFeeStrategy

      ↑
PeakHourFeeStrategy

      ↑
DynamicPricingStrategy
5. Multiple Floors
Interview Answer

Currently the parking lot contains one floor. To support multiple floors, I would change the parking lot to maintain a list of ParkingFloor objects. The allocation strategy can iterate through floors in a preferred order and allocate the first available spot. No changes are required to ParkingSpot or Ticket.

Change

Before

ParkingLot
      ↓
ParkingFloor

After

ParkingLot
      ↓
List<ParkingFloor>
6. Nearest Spot Allocation
Interview Answer

I would not change the parking service. Instead, I would introduce a new implementation of AllocationStrategy called NearestSpotStrategy. It could maintain spots in a priority queue based on distance from the entrance. Since allocation is already pluggable, the rest of the design remains unchanged.

New Class
AllocationStrategy

      ↑

NearestSpotStrategy
7. Random Allocation
Interview Answer

Similar to nearest allocation, I would implement a RandomAllocationStrategy. Only the strategy changes; the parking service and parking floor remain untouched.

8. Multiple Entry/Exit Gates
Interview Answer

I would introduce EntryGate and ExitGate classes. Each gate would delegate requests to the shared ParkingService. Since allocation and checkout are already synchronized using locks, multiple gates can safely operate concurrently.

With multiple entry gates, I would pass the EntryGate as context to the allocation strategy. Instead of searching all floors uniformly, the strategy maintains a preferred list of floors for each gate (for example, Gate 1 → Floors 1, 2, 3 and Gate 2 → Floors 4, 3, 2). This allows us to allocate the nearest available spot while keeping the allocation logic pluggable. ExitGate remains lightweight because the ticket already contains the allocated spot and floor information needed for checkout. This design scales well as we add more gates or floors without changing the core parking workflow.

Classes Added
EntryGate

ExitGate

ParkingService
9. Different Vehicle Types

Suppose tomorrow we add

Bus

Electric Bike

SUV

Luxury Car
Interview Answer

I would extend the Vehicle hierarchy by adding new subclasses and update or introduce a new allocation strategy if these vehicles require different parking rules. The rest of the system remains unchanged.

10. New Spot Types

Suppose tomorrow we add

EV Spot

Handicapped Spot

Compact Spot
Interview Answer

I would add the new spot type to the SpotType enum or create specialized subclasses if they have additional behavior. Since the floor already stores spots as Map<SpotType, Set<ParkingSpot>>, supporting new spot types requires minimal changes.

11. Base + Hourly Pricing
Interview Answer

I would implement another FeeStrategy called BasePlusHourlyFeeStrategy. Since checkout depends only on the FeeStrategy interface, no other class needs modification.

Example:

FeeStrategy

      ↑

BasePlusHourlyFeeStrategy
12. Interview Closing Statement

This is a strong summary to end your design discussion:

"The design is open for extension but closed for modification. New pricing models, allocation algorithms, parking spot types, reservations, EV charging, VIP parking, or additional floors can all be added by introducing new classes or strategy implementations without changing the existing parking workflow. This keeps the design maintainable, scalable, and aligned with the Open/Closed Principle."

