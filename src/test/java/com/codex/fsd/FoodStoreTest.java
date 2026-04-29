package com.codex.fsd;

public final class FoodStoreTest {
    private FoodStoreTest() {
    }

    public static void main(String[] args) {
        createsOrdersWithTotals();
        updatesOrderStatus();
        calculatesMetrics();
        System.out.println("All tests passed.");
    }

    private static void createsOrdersWithTotals() {
        FoodStore store = FoodStore.seeded();
        Order order = store.createOrder("Priya", "10 Main Street", "1:2,3:1", "Ring bell");
        assertEquals(3097, order.totalCents(), "order total");
        assertEquals(OrderStatus.RECEIVED, order.status(), "initial status");
    }

    private static void updatesOrderStatus() {
        FoodStore store = FoodStore.seeded();
        Order order = store.createOrder("Priya", "10 Main Street", "2:1", "");
        Order updated = store.updateStatus(order.id(), OrderStatus.OUT_FOR_DELIVERY);
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, updated.status(), "updated status");
    }

    private static void calculatesMetrics() {
        FoodStore store = FoodStore.seeded();
        Order order = store.createOrder("Priya", "10 Main Street", "6:1", "");
        store.updateStatus(order.id(), OrderStatus.DELIVERED);
        AppMetrics metrics = store.metrics();
        assertEquals(6, metrics.menuItems(), "menu item count");
        assertEquals(2, metrics.openOrders(), "open orders");
        assertEquals(1, metrics.completedOrders(), "completed orders");
        assertEquals(1499, metrics.revenueCents(), "revenue");
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        if (!expected.equals(actual)) {
            throw new AssertionError(name + ": expected " + expected + " but got " + actual);
        }
    }
}
