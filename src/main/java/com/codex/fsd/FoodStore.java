package com.codex.fsd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class FoodStore {
    private final AtomicInteger orderIds = new AtomicInteger(1001);
    private final List<MenuItem> menu = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();

    public static FoodStore seeded() {
        FoodStore store = new FoodStore();
        store.menu.add(new MenuItem(1, "Paneer Tikka Bowl", "Indian", "Smoky paneer, jeera rice, cucumber salad, mint chutney.", 1299, true));
        store.menu.add(new MenuItem(2, "Masala Dosa Wrap", "South Indian", "Crisp dosa-style wrap with potato masala and sambar dip.", 999, true));
        store.menu.add(new MenuItem(3, "Mango Lassi", "Drinks", "Chilled yogurt drink with mango and cardamom.", 499, true));
        store.menu.add(new MenuItem(4, "Street Corn Chaat", "Snacks", "Roasted corn, lime, chili, sev, and cilantro.", 749, true));
        store.menu.add(new MenuItem(5, "Gulab Jamun Cheesecake", "Dessert", "Creamy cheesecake with gulab jamun pieces and saffron syrup.", 699, true));
        store.menu.add(new MenuItem(6, "Tandoori Chicken Plate", "Grill", "Tandoori chicken, naan, salad, and garlic yogurt sauce.", 1499, true));

        store.createOrder("Ava Patel", "742 Market Street", "1:1,3:2", "Leave at reception");
        store.createOrder("Noah Singh", "18 Lake View Apt 4", "2:2,4:1", "Extra chutney");
        store.updateStatus(1001, OrderStatus.PREPARING);
        return store;
    }

    public synchronized List<MenuItem> menu() {
        return menu.stream()
            .filter(MenuItem::available)
            .sorted(Comparator.comparing(MenuItem::category).thenComparing(MenuItem::name))
            .toList();
    }

    public synchronized List<Order> orders() {
        return orders.stream()
            .sorted(Comparator.comparing(Order::id).reversed())
            .toList();
    }

    public synchronized Order createOrder(String customerName, String address, String itemSpec, String notes) {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Delivery address is required");
        }

        List<OrderItem> items = parseItems(itemSpec);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("At least one menu item is required");
        }

        Order order = new Order(
            orderIds.getAndIncrement(),
            customerName.trim(),
            address.trim(),
            notes == null ? "" : notes.trim(),
            OrderStatus.RECEIVED,
            items
        );
        orders.add(order);
        return order;
    }

    public synchronized Order updateStatus(int id, OrderStatus status) {
        for (int index = 0; index < orders.size(); index++) {
            Order order = orders.get(index);
            if (order.id() == id) {
                Order updated = order.withStatus(status);
                orders.set(index, updated);
                return updated;
            }
        }
        throw new NotFoundException("Order " + id + " was not found");
    }

    public synchronized AppMetrics metrics() {
        int openOrders = 0;
        int completedOrders = 0;
        int revenueCents = 0;

        for (Order order : orders) {
            if (order.status() == OrderStatus.DELIVERED) {
                completedOrders++;
                revenueCents += order.totalCents();
            } else {
                openOrders++;
            }
        }

        return new AppMetrics(menu().size(), openOrders, completedOrders, revenueCents);
    }

    private List<OrderItem> parseItems(String itemSpec) {
        List<OrderItem> items = new ArrayList<>();
        if (itemSpec == null || itemSpec.isBlank()) {
            return items;
        }

        for (String token : itemSpec.split(",")) {
            String[] parts = token.trim().split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Order items must use itemId:quantity format");
            }
            int itemId = Integer.parseInt(parts[0]);
            int quantity = Integer.parseInt(parts[1]);
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            MenuItem menuItem = findMenuItem(itemId);
            items.add(new OrderItem(menuItem.id(), menuItem.name(), quantity, menuItem.priceCents()));
        }
        return items;
    }

    private MenuItem findMenuItem(int id) {
        for (MenuItem item : menu) {
            if (item.id() == id && item.available()) {
                return item;
            }
        }
        throw new NotFoundException("Menu item " + id + " was not found");
    }

    public synchronized Order createOrder(Map<String, String> input) {
        return createOrder(input.get("customerName"), input.get("address"), input.get("items"), input.get("notes"));
    }
}
