package ru.gregpack.thewar.network.messages.orders;

import com.google.inject.Singleton;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class OrderQueue {

    private final Deque<Order> orders = new ArrayDeque<>();
    private final Map<Integer, UnitOrder> unitSpecificOrders = new HashMap<>();

    public boolean hasNextOrder() {
        return !orders.isEmpty();
    }

    public Order nextOrder() {
        return orders.removeLast();
    }

    public void addOrder(Order order) {
        synchronized (this) {
            orders.push(order);
            if (!(order instanceof UnitOrder)) {
                this.notify();
                return;
            }
            UnitOrder unitOrder = (UnitOrder) order;
            if (unitSpecificOrders.containsKey(unitOrder.getUnitId())) {
                Order lastOrder = unitSpecificOrders.get(unitOrder.getUnitId());
                orders.remove(lastOrder);
            }
            unitSpecificOrders.put(unitOrder.getUnitId(), unitOrder);
            this.notify();
        }
    }

    public int queueSize() {
        return orders.size();
    }

}
