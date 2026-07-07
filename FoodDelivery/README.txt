Functional Requirements:
Search restaurants by name,cuisine,location
View menu
Manage cart
Place order
Make payment
View order status
Restaurant can update menu and item available

Out of scope:
Delivery partner assignment
Live tracking
Ratings and reviews
Recommendation engines
Restaurant onboarding

Non functional requirements:
High availability
Horizontal scalability
Low-latency Search
Strong consistency for orders and payments
Handle concurrent ordering

Core Entities:
User
Restaurant
menu
MenuItem
order
OrderItem
Cart 
CartItem 
Payment 

Explain the relationship:
User 1->1cart
Restaurant 1->1Menu
Menu 1->N MenuItem
Cart 1->N CartItem 
Order 1->N OrderItem 
Order 1->1 Payment 

Order flow=>
User clicks place order 
Orderservice validates cart
Orderservice creates order 
PaymentService processes payment 
Inventory is reserved 
Orderstatus becomes CONFIRMED
Restaurant notified 

Common follow ups:
How do you prevent invalid transactions?
Use state pattern or transition validator 
boolean canTransition{
    OrderStatus current,
    OrderStatus next
};

Restaurant updates menu while user is checking out 
Scenario:
User added Burger @200
Restaurant updates:
Burder @350
What price should be charged:
You already have priceSnapShot which is excellent.I'd expect Cart stores snapshot.Checkout validates latest price. 
User confirms. or OrderItem snapshot becomes source of truth.

Payment Idempotency
User clicks pay.Network timeout .User clicks pay again.How do you prevent double payment?
Class Payment{
    String paymentId,
    String Idempotency
} or Unique transaction id