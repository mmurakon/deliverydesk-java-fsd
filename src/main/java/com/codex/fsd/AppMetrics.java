package com.codex.fsd;

public record AppMetrics(int menuItems, int openOrders, int completedOrders, int revenueCents) {
    public String toJson() {
        return "{"
            + Json.pair("menuItems", menuItems) + ","
            + Json.pair("openOrders", openOrders) + ","
            + Json.pair("completedOrders", completedOrders) + ","
            + Json.pair("revenueCents", revenueCents)
            + "}";
    }
}
