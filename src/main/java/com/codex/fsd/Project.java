package com.codex.fsd;

import java.time.LocalDate;

public record Project(
    int id,
    String name,
    String owner,
    String description,
    ProjectStatus status,
    Priority priority,
    LocalDate dueDate
) {
    public Project withId(int nextId) {
        return new Project(nextId, name, owner, description, status, priority, dueDate);
    }

    public Project withStatus(ProjectStatus nextStatus) {
        return new Project(id, name, owner, description, nextStatus, priority, dueDate);
    }

    public String toJson() {
        return "{"
            + Json.pair("id", id) + ","
            + Json.pair("name", name) + ","
            + Json.pair("owner", owner) + ","
            + Json.pair("description", description) + ","
            + Json.pair("status", status.name()) + ","
            + Json.pair("priority", priority.name()) + ","
            + Json.pair("dueDate", dueDate.toString())
            + "}";
    }
}
