package com.codex.fsd;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class ApiServer {
    private final HttpServer server;
    private final ProjectStore store;
    private final Path frontendRoot;

    public ApiServer(int port, ProjectStore store, String frontendRoot) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.store = store;
        this.frontendRoot = Path.of(frontendRoot);
        this.server.createContext("/", this::handle);
    }

    public void start() {
        server.start();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (path.equals("/api/projects") && method.equals("GET")) {
                sendJson(exchange, 200, Json.array(store.findAll()));
                return;
            }

            if (path.equals("/api/projects") && method.equals("POST")) {
                Project created = store.create(toProject(readBody(exchange)));
                sendJson(exchange, 201, created.toJson());
                return;
            }

            if (path.startsWith("/api/projects/") && path.endsWith("/status") && method.equals("PATCH")) {
                String idPart = path.substring("/api/projects/".length(), path.length() - "/status".length());
                ProjectStatus status = ProjectStatus.valueOf(Json.object(readBody(exchange)).get("status"));
                Project updated = store.updateStatus(Integer.parseInt(idPart), status);
                sendJson(exchange, 200, updated.toJson());
                return;
            }

            if (path.equals("/api/metrics") && method.equals("GET")) {
                sendJson(exchange, 200, store.metrics().toJson());
                return;
            }

            if (path.startsWith("/api/")) {
                sendJson(exchange, 404, Json.error("Route not found"));
                return;
            }

            serveStatic(exchange, path);
        } catch (IllegalArgumentException exception) {
            sendJson(exchange, 400, Json.error(exception.getMessage()));
        } catch (NotFoundException exception) {
            sendJson(exchange, 404, Json.error(exception.getMessage()));
        } catch (Exception exception) {
            sendJson(exchange, 500, Json.error("Unexpected server error"));
        }
    }

    private Project toProject(String body) {
        Map<String, String> input = Json.object(body);
        return new Project(
            0,
            required(input, "name"),
            required(input, "owner"),
            required(input, "description"),
            ProjectStatus.valueOf(required(input, "status")),
            Priority.valueOf(required(input, "priority")),
            LocalDate.parse(required(input, "dueDate"))
        );
    }

    private String required(Map<String, String> input, String key) {
        String value = input.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value.trim();
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private void serveStatic(HttpExchange exchange, String path) throws IOException {
        Path file = path.equals("/") ? frontendRoot.resolve("index.html") : frontendRoot.resolve(path.substring(1));
        Path normalized = file.normalize();
        if (!normalized.startsWith(frontendRoot) || !Files.exists(normalized) || Files.isDirectory(normalized)) {
            sendJson(exchange, 404, Json.error("File not found"));
            return;
        }

        byte[] bytes = Files.readAllBytes(normalized);
        exchange.getResponseHeaders().set("Content-Type", contentType(normalized));
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private String contentType(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (name.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        return "application/octet-stream";
    }
}
