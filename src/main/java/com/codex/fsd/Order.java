package com.codex.fsd;

import java.util.ArrayList;
import java.util.List;

public record Order(
    int id,
    String customerName,
    String address,
    String notes,
    OrderStatus status,
    List<OrderItem> items
) {
    public Order {
        items = List.copyOf(items);
    }

    public Order withStatus(OrderStatus nextStatus) {
        return new Order(id, customerName, address, notes, nextStatus, items);
    }

    public int totalCents() {
        int total = 0;
        for (OrderItem item : items) {
            total += item.subtotalCents();
        }
        return total;
    }

    public String toJson() {
        List<String> itemJson = new ArrayList<>();
        for (OrderItem item : items) {
            itemJson.add(item.toJson());
        }

        return "{"
            + Json.pair("id", id) + ","
            + Json.pair("customerName", customerName) + ","
            + Json.pair("address", address) + ","
            + Json.pair("notes", notes) + ","
            + Json.pair("status", status.name()) + ","
            + Json.pair("totalCents", totalCents()) + ","
            + Json.rawPair("items", Json.arrayValues(itemJson))
            + "}";
    }
}
