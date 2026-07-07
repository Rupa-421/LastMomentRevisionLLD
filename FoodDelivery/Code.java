interface PaymentStrategy{
    PaymentResult pay(double amount);
}
Implementation:
CreditCardPayment 
UPIPayment
NetBankingPayment

class OrderService{
    private PaymentService paymentService;
    private InventoryService inventoryService;
    public Order buildOrder(Cart cart){
            Order order = new Order();
            for(CartItem cartItem : cart.getItems()){
                MenuItem latest =
                        menuRepository.get(cartItem.getItemId());
                if(latest.getPrice()
                        != cartItem.getPriceSnapshot()){
                    throw new PriceChangedException();
                }
                OrderItem orderItem =
                        new OrderItem(
                                cartItem.getItemId(),
                                cartItem.getQuantity(),
                                cartItem.getPriceSnapshot());
                order.add(orderItem);
                }
        return order;
    }
    public Order createOrder(Cart cart,PaymentStrategy paymentStrategy){
        validateCart(cart);
        Order order = buildOrder(cart);
        boolean inventoryReserved = inventoryService.reserveInventory(order);
        if(!inventoryReserved){
            throw new InventoryNotAvailableException();
        }
        PaymentStatus paymentStatus = paymentService.processPayment(order,paymentStrategy);
        if(paymentStatus != PaymentStatus.SUCCESS){
            inventoryService.releaseInventory(order);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            return order;
    }
        order.setStatus(OrderStatus.CONFIRMED);
        return order;
    }
}

class InventoryService{
    Lock lock = new ReentrantLock();
    public boolean reserveInventory(Order order){
        lock.lock();
        try{
        for(OrderItem item: order.getItems()){
            if(item.getMenuItem().getAvailableQuantity() < item.getQuantity()){
                return false;
            }
        }
        for(OrderItem item: order.getItems()){
            item.getMenuItem().decreaseAvailableQuantity(item.getQuantity());
        }}
        finally{
            lock.unlock();
        }
        return true;
    }
    
    public void releaseInventory(Order order){
        // Logic to release inventory
    }
}

class Order{
    private OrderStatus status;
    public void updateStatus(OrderStatus nextStatus){
        if(!validTransition(this.status,nextStatus)){
            throw new InvalidOrderStatusTransitionException();
        }
        this.status = nextStatus;
    }
}

class CartService{
    public void addItem(Cart cart ,MenuItem item,int quantity){
        if(!item.isActive()){
            throw new InactiveMenuItemException();
        }
        CartItem cartItem = new CartItem(
                item.getItemId(),
                quantity,
                item.getPrice()      // snapshot
        );

        cart.addItem(cartItem,quantity);
    }
    public void removeItem(Cart cart ,MenuItem item){
        cart.removeItem(item);
    }
    public double calculatePrice(Cart cart){
        return cart.calculateTotalPrice();
    }
}

class RestaurantManager{
    List<Restaurant> restaurants;
    public List<Restaurant> search(SearchCriteria criteria){
        // Logic to search restaurants based on criteria
        List<Restaurant> result = new ArrayList<>();
        for(Restaurant restaurant: restaurants){
            if(restaurant.matches(criteria)){
                result.add(restaurant);
            }
        }
        return result;
    }
    public Menu getMenu(Restaurant restaurant){
        return restaurant.getMenu();
    }
    public boolean updateAvailability(String itemId,boolean isActive){
        for(Restaurant restaurant: restaurants){
            MenuItem item = restaurant.getMenu().getItemById(itemId);
            if(item != null){
                item.setActive(isActive);
                return true;
            }
        }
        return false;
    }
}
