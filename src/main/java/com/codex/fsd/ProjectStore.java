package com.codex.fsd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class ProjectStore {
    private final AtomicInteger ids = new AtomicInteger(1);
    private final List<Project> projects = new ArrayList<>();

    public static ProjectStore seeded() {
        ProjectStore store = new ProjectStore();
        store.create(new Project(0, "Claims Dashboard", "Maya", "Ship operational dashboards for claims adjusters.", ProjectStatus.IN_PROGRESS, Priority.HIGH, LocalDate.now().plusDays(12)));
        store.create(new Project(0, "API Gateway Cleanup", "Dev", "Consolidate auth filters and normalize service responses.", ProjectStatus.PLANNED, Priority.MEDIUM, LocalDate.now().plusDays(24)));
        store.create(new Project(0, "Mobile Release Fixes", "Priya", "Resolve checkout blockers before the next app release.", ProjectStatus.BLOCKED, Priority.HIGH, LocalDate.now().plusDays(6)));
        store.create(new Project(0, "Reporting Export", "Noah", "Add CSV export and audit history for finance reports.", ProjectStatus.DONE, Priority.LOW, LocalDate.now().minusDays(2)));
        return store;
    }

    public synchronized List<Project> findAll() {
        return projects.stream()
            .sorted(Comparator.comparing(Project::dueDate))
            .toList();
    }

    public synchronized Project create(Project project) {
        validate(project);
        Project created = project.withId(ids.getAndIncrement());
        projects.add(created);
        return created;
    }

    public synchronized Project updateStatus(int id, ProjectStatus status) {
        for (int index = 0; index < projects.size(); index++) {
            Project project = projects.get(index);
            if (project.id() == id) {
                Project updated = project.withStatus(status);
                projects.set(index, updated);
                return updated;
            }
        }
        throw new NotFoundException("Project " + id + " was not found");
    }

    public synchronized Metrics metrics() {
        int active = 0;
        int done = 0;
        int highPriority = 0;

        for (Project project : projects) {
            if (project.status() == ProjectStatus.IN_PROGRESS || project.status() == ProjectStatus.BLOCKED) {
                active++;
            }
            if (project.status() == ProjectStatus.DONE) {
                done++;
            }
            if (project.priority() == Priority.HIGH) {
                highPriority++;
            }
        }

        return new Metrics(projects.size(), active, done, highPriority);
    }

    private void validate(Project project) {
        if (project.name().isBlank()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (project.owner().isBlank()) {
            throw new IllegalArgumentException("Project owner is required");
        }
        if (project.description().isBlank()) {
            throw new IllegalArgumentException("Project description is required");
        }
    }
}
