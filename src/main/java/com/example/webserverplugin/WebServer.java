package com.example.webserverplugin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public class WebServer {
    private final JavaPlugin plugin;
    private final int port;
    private final String htmlDirectory;
    private HttpServer server;

    public WebServer(JavaPlugin plugin, int port, String htmlDirectory) {
        this.plugin = plugin;
        this.port = port;
        this.htmlDirectory = htmlDirectory;
    }

    public void start() throws IOException {
        Path htmlPath = plugin.getDataFolder().toPath().resolve(htmlDirectory);
        if (!Files.exists(htmlPath)) {
            plugin.getLogger().warning("HTML路径没找到,已经创建: " + htmlPath);
            Files.createDirectories(htmlPath);
        }

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new FileHandler(htmlPath));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static class FileHandler implements HttpHandler {
        private final Path basePath;

        public FileHandler(Path basePath) {
            this.basePath = basePath;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            File file = basePath.resolve(path.substring(1)).normalize().toFile();

            if (!file.exists() || file.isDirectory() || !file.getCanonicalPath().startsWith(basePath.toFile().getCanonicalPath())) {
                sendResponse(exchange, 404, "text/plain", "404 Not Found");
                return;
            }

            byte[] content = Files.readAllBytes(file.toPath());
            sendResponse(exchange, 200, getContentType(file), content);
        }

        private String getContentType(File file) {
            String fileName = file.getName();
            if (fileName.endsWith(".html")) return "text/html";
            if (fileName.endsWith(".css")) return "text/css";
            if (fileName.endsWith(".js")) return "application/javascript";
            return "application/octet-stream";
        }

        private void sendResponse(HttpExchange exchange, int code, String contentType, byte[] content) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(code, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }

        private void sendResponse(HttpExchange exchange, int code, String contentType, String message) throws IOException {
            sendResponse(exchange, code, contentType, message.getBytes());
        }
    }
}