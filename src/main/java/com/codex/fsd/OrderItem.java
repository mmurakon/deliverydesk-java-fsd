package com.codex.fsd;

public record OrderItem(int menuItemId, String name, int quantity, int unitPriceCents) {
    public int subtotalCents() {
        return quantity * unitPriceCents;
    }

    public String toJson() {
        return "{"
            + Json.pair("menuItemId", menuItemId) + ","
            + Json.pair("name", name) + ","
            + Json.pair("quantity", quantity) + ","
            + Json.pair("unitPriceCents", unitPriceCents) + ","
            + Json.pair("subtotalCents", subtotalCents())
            + "}";
    }
}
