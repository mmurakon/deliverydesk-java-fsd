package com.codex.fsd;

public record Metrics(int total, int active, int done, int highPriority) {
    public String toJson() {
        return "{"
            + Json.pair("total", total) + ","
            + Json.pair("active", active) + ","
            + Json.pair("done", done) + ","
            + Json.pair("highPriority", highPriority)
            + "}";
    }
}
