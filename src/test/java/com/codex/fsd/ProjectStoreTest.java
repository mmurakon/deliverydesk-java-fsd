package com.codex.fsd;

import java.time.LocalDate;

public final class ProjectStoreTest {
    private ProjectStoreTest() {
    }

    public static void main(String[] args) {
        createsProjectsWithIds();
        updatesStatus();
        calculatesMetrics();
        System.out.println("All tests passed.");
    }

    private static void createsProjectsWithIds() {
        ProjectStore store = new ProjectStore();
        Project created = store.create(sample("Portal", ProjectStatus.PLANNED, Priority.MEDIUM));
        assertEquals(1, created.id(), "first id");
        assertEquals(1, store.findAll().size(), "project count");
    }

    private static void updatesStatus() {
        ProjectStore store = new ProjectStore();
        Project created = store.create(sample("Portal", ProjectStatus.PLANNED, Priority.MEDIUM));
        Project updated = store.updateStatus(created.id(), ProjectStatus.DONE);
        assertEquals(ProjectStatus.DONE, updated.status(), "updated status");
    }

    private static void calculatesMetrics() {
        ProjectStore store = new ProjectStore();
        store.create(sample("One", ProjectStatus.IN_PROGRESS, Priority.HIGH));
        store.create(sample("Two", ProjectStatus.BLOCKED, Priority.HIGH));
        store.create(sample("Three", ProjectStatus.DONE, Priority.LOW));
        Metrics metrics = store.metrics();
        assertEquals(3, metrics.total(), "total");
        assertEquals(2, metrics.active(), "active");
        assertEquals(1, metrics.done(), "done");
        assertEquals(2, metrics.highPriority(), "high priority");
    }

    private static Project sample(String name, ProjectStatus status, Priority priority) {
        return new Project(0, name, "Priya", "Sample work", status, priority, LocalDate.of(2026, 5, 20));
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        if (!expected.equals(actual)) {
            throw new AssertionError(name + ": expected " + expected + " but got " + actual);
        }
    }
}
