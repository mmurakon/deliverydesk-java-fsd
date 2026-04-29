package com.codex.fsd;

public record MenuItem(
    int id,
    String name,
    String category,
    String description,
    int priceCents,
    boolean available
) {
    public String toJson() {
        return "{"
            + Json.pair("id", id) + ","
            + Json.pair("name", name) + ","
            + Json.pair("category", category) + ","
            + Json.pair("description", description) + ","
            + Json.pair("priceCents", priceCents) + ","
            + Json.pair("available", available)
            + "}";
    }
}
