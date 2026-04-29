package com.codex.fsd;

public final class App {
    private App() {
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        FoodStore store = FoodStore.seeded();
        ApiServer server = new ApiServer(port, store, "frontend");
        server.start();
        System.out.printf("BiteFlow is running at http://localhost:%d%n", port);
    }
}
